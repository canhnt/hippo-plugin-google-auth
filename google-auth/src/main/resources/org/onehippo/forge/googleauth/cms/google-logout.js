function onLoad() {
  gapi.load('auth2', function() {
    gapi.auth2.init();
  });

  // subscribe the 'logout' event from hippo message bus
  Hippo.Events.subscribe('CMSLogout', function () {
    var auth2 = gapi.auth2.getAuthInstance();
    auth2.signOut();
  });
}
