function submitGoogleUser (googleUser) {
  var profile = googleUser.getBasicProfile(),
    id_token = googleUser.getAuthResponse().id_token;
  Wicket.Ajax.post({
    u: "${callbackUrl}",
    ep: {
      id: profile.getId(),
      name: profile.getName(),
      email: profile.getEmail(),
      id_token: id_token
    }
  });
}