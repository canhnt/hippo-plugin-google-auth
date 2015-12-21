/*
 *  Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.plugins.console.icons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.util.JcrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hippoecm.repository.api.HippoNodeType.CONFIGURATION_PATH;
import static org.hippoecm.repository.HippoStdNodeType.HIPPOSTD_STATE;
import static org.hippoecm.repository.HippoStdNodeType.NT_DIRECTORY;
import static org.hippoecm.repository.HippoStdNodeType.NT_FOLDER;

public class JcrNodeIcon {

    static final Logger log = LoggerFactory.getLogger(JcrNodeIcon.class);

    private static final String CONTENT_ROOT = "/content";
    private static final String NODE_CSS_CLASS_PREFIX = "jcrnode-";
    private static final String DEFAULT_NODE_CSS_CLASS = NODE_CSS_CLASS_PREFIX + "default";
    private static final String VIRTUAL_NODE_CSS_CLASS = NODE_CSS_CLASS_PREFIX + "virtual";
    private static final String FACNAV_NODE_CSS_CLASS = NODE_CSS_CLASS_PREFIX + "facnav";

    private static final String FA_CSS_CLASS = "fa";
    private static final String FA_DEFAULT_CSS_CLASS = FontAwesomeIcon.CIRCLE.cssClass();
    private static final String FA_VIRTUAL_NODE_CSS_CLASS = FontAwesomeIcon.CIRCLE_O.cssClass();

    public static final String FA_DEFAULT_NODE_CSS_CLASS = FA_CSS_CLASS + " " + FA_DEFAULT_CSS_CLASS + " " + DEFAULT_NODE_CSS_CLASS;

    private static final List<String> nodeTypes = new ArrayList<>();
    private static final Map<String, String> pathCssClass = new HashMap<>();
    private static final Map<String, FontAwesomeIcon> primaryTypeNameIcons = new HashMap<>();

    static {
        nodeTypes.add("hippostd:publishable");

        // hst config
        primaryTypeNameIcons.put("hst:hst", FontAwesomeIcon.CLOUD);
        primaryTypeNameIcons.put("hst:sitemapitem", FontAwesomeIcon.SITEMAP);
        primaryTypeNameIcons.put("hst:sitemap", FontAwesomeIcon.SITEMAP);
        primaryTypeNameIcons.put("hst:template", FontAwesomeIcon.FILE_TEXT_O);
        primaryTypeNameIcons.put("hst:templates", FontAwesomeIcon.FILE_O);
        primaryTypeNameIcons.put("hst:component", FontAwesomeIcon.PUZZLE_PIECE);
        primaryTypeNameIcons.put("hst:components", FontAwesomeIcon.PUZZLE_PIECE);
        primaryTypeNameIcons.put("hst:abstractcomponent", FontAwesomeIcon.PUZZLE_PIECE);
        primaryTypeNameIcons.put("hst:blueprint", FontAwesomeIcon.DROPBOX);
        primaryTypeNameIcons.put("hst:blueprints", FontAwesomeIcon.DROPBOX);
        primaryTypeNameIcons.put("hst:channels", FontAwesomeIcon.LAPTOP);
        primaryTypeNameIcons.put("hst:channel", FontAwesomeIcon.LAPTOP);
        primaryTypeNameIcons.put("hst:channelinfo", FontAwesomeIcon.INFO_CIRCLE);
        primaryTypeNameIcons.put("hst:sites", FontAwesomeIcon.STAR_O);
        primaryTypeNameIcons.put("hst:site", FontAwesomeIcon.STAR);
        primaryTypeNameIcons.put("hst:pages", FontAwesomeIcon.COPY);
        primaryTypeNameIcons.put("hst:workspace", FontAwesomeIcon.WRENCH);
        primaryTypeNameIcons.put("hst:catalog", FontAwesomeIcon.DROPBOX);
        primaryTypeNameIcons.put("hst:configuration", FontAwesomeIcon.COG);
        primaryTypeNameIcons.put("hst:configurations", FontAwesomeIcon.COGS);
        primaryTypeNameIcons.put("hst:mount", FontAwesomeIcon.ROCKET);
        primaryTypeNameIcons.put("hst:virtualhosts", FontAwesomeIcon.ARROW_CIRCLE_DOWN);
        primaryTypeNameIcons.put("hst:virtualhostgroup", FontAwesomeIcon.ARROW_CIRCLE_O_RIGHT);
        primaryTypeNameIcons.put("hst:virtualhost", FontAwesomeIcon.ARROW_CIRCLE_RIGHT);
        primaryTypeNameIcons.put("hippogallery:stdAssetGallery", FontAwesomeIcon.PAPERCLIP);
        primaryTypeNameIcons.put("hippogallery:stdImageGallery", FontAwesomeIcon.PICTURE_O);
        primaryTypeNameIcons.put("hst:containeritemcomponent", FontAwesomeIcon.CUBE);
        primaryTypeNameIcons.put("hst:containeritempackage", FontAwesomeIcon.DROPBOX);
        primaryTypeNameIcons.put("hst:containercomponent", FontAwesomeIcon.COLUMNS);
        primaryTypeNameIcons.put("hst:containercomponentfolder", FontAwesomeIcon.TRELLO);
        primaryTypeNameIcons.put("hst:sitemenus", FontAwesomeIcon.TH_LIST);
        primaryTypeNameIcons.put("hst:sitemenu", FontAwesomeIcon.TH_LIST);
        primaryTypeNameIcons.put("hst:sitemenuitem", FontAwesomeIcon.ALIGN_JUSTIFY);

        // content
        primaryTypeNameIcons.put("hippo:handle", FontAwesomeIcon.UMBRELLA);
        primaryTypeNameIcons.put("hippostd:publishable", FontAwesomeIcon.FILE_TEXT);
        primaryTypeNameIcons.put("hippo:translation", FontAwesomeIcon.FLAG_O);
        primaryTypeNameIcons.put("hippotranslation:translations", FontAwesomeIcon.RANDOM);
        primaryTypeNameIcons.put(NT_FOLDER, FontAwesomeIcon.FOLDER_O);
        primaryTypeNameIcons.put(NT_DIRECTORY, FontAwesomeIcon.FOLDER);
        primaryTypeNameIcons.put("hippofacnav:facetnavigation", FontAwesomeIcon.DRIBBBLE);
        primaryTypeNameIcons.put("hst:formdatacontainer", FontAwesomeIcon.SEND);
        primaryTypeNameIcons.put("hst:formdata", FontAwesomeIcon.SEND_O);
        primaryTypeNameIcons.put("hippotaxonomy:container", FontAwesomeIcon.BOOK);
        primaryTypeNameIcons.put("urlrewriter:ruleset", FontAwesomeIcon.CROSSHAIRS);

        // config
        primaryTypeNameIcons.put("webfiles:webfiles", FontAwesomeIcon.SUITCASE);
        primaryTypeNameIcons.put("webfiles:bundle", FontAwesomeIcon.SUITCASE);
        primaryTypeNameIcons.put("nt:folder", FontAwesomeIcon.FOLDER_O);
        primaryTypeNameIcons.put("nt:file", FontAwesomeIcon.FILE_TEXT_O);
        primaryTypeNameIcons.put("hipposys:configuration", FontAwesomeIcon.COGS);
        primaryTypeNameIcons.put("hipposysedit:namespacefolder", FontAwesomeIcon.BULLSEYE);
        primaryTypeNameIcons.put("hipposysedit:namespace", FontAwesomeIcon.BULLSEYE);
        primaryTypeNameIcons.put("frontend:plugin", FontAwesomeIcon.PLUG);
        primaryTypeNameIcons.put("frontend:pluginconfig", FontAwesomeIcon.COG);
        primaryTypeNameIcons.put("frontend:plugincluster", FontAwesomeIcon.COGS);
        primaryTypeNameIcons.put("editor:templateset", FontAwesomeIcon.FILE_TEXT_O);
        primaryTypeNameIcons.put("hipposysedit:prototypeset", FontAwesomeIcon.STAR_O);
        primaryTypeNameIcons.put("hipposysedit:templatetype", FontAwesomeIcon.FILE_TEXT);
        primaryTypeNameIcons.put("hipposys:group", FontAwesomeIcon.USERS);
        primaryTypeNameIcons.put("hipposys:groupfolder", FontAwesomeIcon.USERS);
        primaryTypeNameIcons.put("hipposys:userfolder", FontAwesomeIcon.USER);
        primaryTypeNameIcons.put("hipposys:user", FontAwesomeIcon.USER);
        primaryTypeNameIcons.put("hipposys:workflowfolder", FontAwesomeIcon.REFRESH);
        primaryTypeNameIcons.put("hipposys:workflowcategory", FontAwesomeIcon.REFRESH);
        primaryTypeNameIcons.put("hipposys:workflow", FontAwesomeIcon.REFRESH);
        primaryTypeNameIcons.put("hipposys:update", FontAwesomeIcon.WRENCH);
        primaryTypeNameIcons.put("hipposys:applicationfolder", FontAwesomeIcon.DIAMOND);
        primaryTypeNameIcons.put("hipposys:modulefolder", FontAwesomeIcon.SIMPLYBUILT);
        primaryTypeNameIcons.put("hipposys:queryfolder", FontAwesomeIcon.QUESTION_CIRCLE);
        primaryTypeNameIcons.put("hipposys:queryfolder", FontAwesomeIcon.QUESTION_CIRCLE);
        primaryTypeNameIcons.put("hippostd:templatequery", FontAwesomeIcon.QUESTION);

        // targeting
        primaryTypeNameIcons.put("targeting:experiments", FontAwesomeIcon.FLASK);
        primaryTypeNameIcons.put("targeting:experiment", FontAwesomeIcon.FLASK);
        primaryTypeNameIcons.put("targeting:experimentsfolder", FontAwesomeIcon.FOLDER_O);
        primaryTypeNameIcons.put("targeting:configuration", FontAwesomeIcon.DASHBOARD);
        primaryTypeNameIcons.put("targeting:collectors", FontAwesomeIcon.STEAM);
        primaryTypeNameIcons.put("targeting:collector", FontAwesomeIcon.STEAM);
        primaryTypeNameIcons.put("targeting:personas", FontAwesomeIcon.STREETVIEW);
        primaryTypeNameIcons.put("targeting:persona", FontAwesomeIcon.STREETVIEW);
        primaryTypeNameIcons.put("targeting:goals", FontAwesomeIcon.ASTERISK);
        primaryTypeNameIcons.put("targeting:goal", FontAwesomeIcon.ASTERISK);
        primaryTypeNameIcons.put("targeting:characteristics", FontAwesomeIcon.SLACK);
        primaryTypeNameIcons.put("targeting:characteristic", FontAwesomeIcon.SLACK);
        primaryTypeNameIcons.put("targeting:datastores", FontAwesomeIcon.DATABASE);
        primaryTypeNameIcons.put("targeting:datastore", FontAwesomeIcon.DATABASE);
        primaryTypeNameIcons.put("targeting:alteregos", FontAwesomeIcon.USER_SECRET);
        primaryTypeNameIcons.put("targeting:alterego", FontAwesomeIcon.USER_SECRET);
        primaryTypeNameIcons.put("targeting:dataflow", FontAwesomeIcon.LOCK);
        primaryTypeNameIcons.put("targeting:lock", FontAwesomeIcon.LOCK);
        primaryTypeNameIcons.put("targeting:statistics", FontAwesomeIcon.BAR_CHART);
        primaryTypeNameIcons.put("targeting:statistic", FontAwesomeIcon.BAR_CHART);
        primaryTypeNameIcons.put("targeting:trends", FontAwesomeIcon.LINE_CHART);
        primaryTypeNameIcons.put("targeting:facets", FontAwesomeIcon.LINE_CHART);
        primaryTypeNameIcons.put("targeting:conversions", FontAwesomeIcon.SMILE_O);
        primaryTypeNameIcons.put("targeting:events", FontAwesomeIcon.CLOCK_O);
        primaryTypeNameIcons.put("targeting:event", FontAwesomeIcon.CLOCK_O);
        primaryTypeNameIcons.put("targeting:andexpression", FontAwesomeIcon.ANGLE_DOUBLE_RIGHT);
        primaryTypeNameIcons.put("targeting:orexpression", FontAwesomeIcon.ANGLE_DOUBLE_RIGHT);
        primaryTypeNameIcons.put("targeting:expression", FontAwesomeIcon.ANGLE_DOUBLE_RIGHT);
        primaryTypeNameIcons.put("targeting:negate", FontAwesomeIcon.ANGLE_DOUBLE_RIGHT);
        primaryTypeNameIcons.put("targeting:abstractandexpression", FontAwesomeIcon.ANGLE_DOUBLE_RIGHT);

        pathCssClass.put("/hst:hst", "hst");
        pathCssClass.put("/" + CONFIGURATION_PATH, "conf");
        pathCssClass.put("/content", "content");
        pathCssClass.put("/hippo:namespaces", "namespaces");
        pathCssClass.put("/formdata", "formdata");
        pathCssClass.put("/webfiles", "webfiles");
        pathCssClass.put("/hippo:reports", "reports");
        pathCssClass.put("/hippo:log", "log");
        pathCssClass.put("/targeting:targeting", "targeting");
    }

    /**
     * Compute all CSS classes for the specified node.

     * @param node The node to compute the CSS classes for
     * @return a String containing all CSS classes for the specified node
     */
    public static String getIconCssClass(Node node) {
        return FA_CSS_CLASS + " " + getIconTypeCssClass(node) + " " + getIconColorCssClass(node);
    }

    /**
     * Get the Font-Awesome CSS class for the specified node based on the mapping in primaryTypeNameIcons, or the
     * default if no mapping is found.
     *
     * @param node The node to retrieve the Font-Awesome CSS class for
     * @return the Font-Awesome CSS class for the specified node
     */
    private static String getIconTypeCssClass(final Node node) {
        // types first
        for (String nodeType : nodeTypes) {
            if (isNodeType(node, nodeType) && primaryTypeNameIcons.containsKey(nodeType)) {
                return primaryTypeNameIcons.get(nodeType).cssClass();
            }
        }

        final String name = getPrimaryTypeName(node);
        if (primaryTypeNameIcons.containsKey(name)) {
            return primaryTypeNameIcons.get(name).cssClass();
        }

        if (isVirtual(node)) {
            return FA_VIRTUAL_NODE_CSS_CLASS;
        }

        return FA_DEFAULT_CSS_CLASS;
    }

    /**
     * Get the CSS class for to the specified node.
     *
     * @param node The node to retrieve the CSS class for
     * @return The CSS class related to the specified node or the default CSS class if no mapping is found or an error
     * occurs
     */
    private static String getIconColorCssClass(final Node node) {
        try {
            final String path = node.getPath();
            if (path.startsWith(CONTENT_ROOT) && node.hasProperty(HIPPOSTD_STATE)) {
                return NODE_CSS_CLASS_PREFIX + node.getProperty(HIPPOSTD_STATE).getString();
            }

            if (isNodeType(node, "hippofacnav:facetnavigation")) {
                return FACNAV_NODE_CSS_CLASS;
            }

            if (isVirtual(node)) {
                return VIRTUAL_NODE_CSS_CLASS;
            }

            for (Map.Entry<String, String> pathColor : pathCssClass.entrySet()) {
                if (path.startsWith(pathColor.getKey())) {
                    return NODE_CSS_CLASS_PREFIX + pathColor.getValue();
                }
            }
        } catch (RepositoryException e) {
            // ignore, use default color
        }
        return DEFAULT_NODE_CSS_CLASS;
    }

    /**
     * Retrieve the primary type name of the specified node or an empty String if an error occurs
     *
     * @param node The node to retrieve the primary node type name from
     * @return The primary type name of the node or an empty String if an error occurs
     */
    private static String getPrimaryTypeName(final Node node) {
        try {
            return node.getPrimaryNodeType().getName();
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Checks if the node is of the specified type
     *
     * @param node The node to check the type of
     * @param type The type to check the node against
     * @return true if the node is of the specified type, or false if not or an error occurs
     */
    private static boolean isNodeType(final Node node, final String type) {
        try {
            return node.isNodeType(type);
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Checks if the node is a virtual node
     *
     * @return true if the node is virtual else false
     */
    private static boolean isVirtual(Node node) {
        try {
            return JcrUtils.isVirtual(node);
        } catch (RepositoryException e) {
            log.info("Cannot determine whether node '{}' is virtual, assuming it's not", JcrUtils.getNodePathQuietly(node), e);
        }
        return false;
    }

    private JcrNodeIcon() {}
}
