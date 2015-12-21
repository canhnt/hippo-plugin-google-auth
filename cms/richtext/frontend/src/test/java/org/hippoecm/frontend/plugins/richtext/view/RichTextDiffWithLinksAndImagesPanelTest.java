/*
 * Copyright 2013-2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.plugins.richtext.view;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.mock.MockHomePage;
import org.apache.wicket.model.IModel;
import org.easymock.EasyMock;
import org.hippoecm.frontend.HippoTester;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.richtext.IHtmlCleanerService;
import org.hippoecm.frontend.plugins.richtext.htmlcleaner.HtmlCleanerPlugin;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNodeType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onehippo.repository.mock.MockNode;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link RichTextDiffWithLinksAndImagesPanel}.
 */
public class RichTextDiffWithLinksAndImagesPanelTest {

    private static IHtmlCleanerService CLEANER;

    private MockNode root;
    private javax.jcr.Session session;

    @BeforeClass
    public static void staticSetUp() {
        IPluginConfig cleanerConfig = EasyMock.createMock(IPluginConfig.class);
        expect(cleanerConfig.getString(eq("charset"), eq("UTF-8"))).andReturn("UTF-8");
        expect(cleanerConfig.getString(eq("serializer"), eq("simple"))).andReturn("simple");
        expect(cleanerConfig.getBoolean(eq("omitComments"))).andReturn(false);
        expect(cleanerConfig.get(eq("filter"))).andReturn(false);
        EasyMock.replay(cleanerConfig);
        CLEANER = new HtmlCleanerPlugin(null, cleanerConfig);
    }

    @Before
    public void setUp() throws RepositoryException, NoSuchMethodException {
        final HippoTester tester = new HippoTester();
        tester.startPage(MockHomePage.class);

        session = UserSession.get().getJcrSession();
        root = (MockNode) session.getRootNode();
    }

    @Test
    public void sameTextShowsNoChanges() throws RepositoryException {
        Node base = addHtmlNode(root, "base", "Some text");
        Node current = addHtmlNode(root, "current", "Some text");
        String diff = createDiff(base, current);
        assertEquals(htmlEncode("<html>Some text</html>\n"), diff);
    }

    @Test
    public void sameCharacterDifferentEncodingShowsNoChanges() throws RepositoryException {
        Node base = addHtmlNode(root, "base", "e &#101; ' ' &apos; &apos; &#39; &#39;");
        Node current = addHtmlNode(root, "current", "&#101; e &apos; &#39; ' &#39; ' &apos;");
        String diff = createDiff(base, current);
        assertEquals(htmlEncode("<html>e e &#039; &#039; &#039; &#039; &#039; &#039;</html>\n"), diff);
    }

    @Test
    public void differentTextShowsChanges() throws RepositoryException {
        Node base = addHtmlNode(root, "base", "aap");
        Node current = addHtmlNode(root, "current", "noot");
        String diff = createDiff(base, current);
        assertEquals(htmlEncode("<html>\n"
                + "<span class=\"diff-html-removed\" id=\"removed-null-0\" previous=\"first-null\" changeId=\"removed-null-0\" next=\"added-null-0\">aap</span>"
                + "<span class=\"diff-html-added\" id=\"added-null-0\" previous=\"removed-null-0\" changeId=\"added-null-0\" next=\"last-null\">noot</span>\n"
                + "</html>\n"), diff);
    }

    @Test
    public void addedImageShowsChanges() throws RepositoryException {
        MockNode imageHandle = addImage(root, "image.jpg");

        Node base = addHtmlNode(root, "base", "text");

        Node current = addHtmlNode(root, "current", "text <img src=\"image.jpg/{_document}/hippogallery:thumbnail\"/>");
        addFacet(current, imageHandle);

        String diff = createDiff(base, current);

        assertEquals(htmlEncode("<html>text "
                + "<span class=\"diff-html-added\" id=\"added-null-0\" previous=\"first-null\" changeId=\"added-null-0\" next=\"last-null\">"
                + "<img src=\"binaries/image.jpg/image.jpg/hippogallery:thumbnail\" data-uuid=\""+ imageHandle.getIdentifier() +"\" data-type=\"hippogallery:thumbnail\" changeType=\"diff-added-image\">"
                + "</span>\n"
                + "</html>\n"), diff);
    }

    @Test
    public void removedImageShowsChanges() throws RepositoryException {
        MockNode imageHandle = addImage(root, "image.jpg");

        Node base = addHtmlNode(root, "base", "text <img src=\"image.jpg/{_document}/hippogallery:thumbnail\"/>");
        addFacet(base, imageHandle);

        Node current = addHtmlNode(root, "current", "text");

        String diff = createDiff(base, current);

        assertEquals(htmlEncode("<html>text"
                + "<span class=\"diff-html-removed\" previous=\"first-null\" changeId=\"removed-null-0\" next=\"last-null\"> </span>"
                + "<span class=\"diff-html-removed\" id=\"removed-null-0\" previous=\"first-null\" changeId=\"removed-null-0\" next=\"last-null\">"
                + "<img src=\"binaries/image.jpg/image.jpg/hippogallery:thumbnail\" data-uuid=\""+ imageHandle.getIdentifier() +"\" data-type=\"hippogallery:thumbnail\" changeType=\"diff-removed-image\">"
                + "</span>\n"
                + "</html>\n"), diff);
    }

    private MockNode addImage(final MockNode node, final String name) throws RepositoryException {
        MockNode imageHandle = node.addMockNode(name, "hippo:handle");
        MockNode imageSet = imageHandle.addMockNode(name, "hippogallery:imageset");
        imageSet.setPrimaryItemName("hippogallery:thumbnail");
        imageSet.addNode("hippogallery:thumbnail", "hippo:resource");
        return imageHandle;
    }

    private void addFacet(final Node current, final MockNode imageHandle) throws RepositoryException {
        Node facet = current.addNode("image.jpg", HippoNodeType.NT_FACETSELECT);
        facet.setProperty(HippoNodeType.HIPPO_DOCBASE, imageHandle.getIdentifier());
    }

    private static Node addHtmlNode(Node node, String name, String content) throws RepositoryException {
        Node htmlNode = node.addNode(name, HippoStdNodeType.NT_HTML);
        htmlNode.setProperty(HippoStdNodeType.HIPPOSTD_CONTENT, content);
        return htmlNode;
    }

    private static String createDiff(Node base, Node current) {
        RichTextDiffWithLinksAndImagesPanel panel = new RichTextDiffWithLinksAndImagesPanel("id",
                createNodeModel(base), createNodeModel(current), null, null, CLEANER);
        return panel.get(RichTextDiffWithLinksAndImagesPanel.WICKET_ID_VIEW).getDefaultModelObjectAsString();
    }

    private static IModel<Node> createNodeModel(Node node) {
        IModel<Node> model = createMock(IModel.class);
        expect(model.getObject()).andReturn(node).anyTimes();
        replay(model);
        return model;
    }

    private static String htmlEncode(final String text) {
        return text.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }

}
