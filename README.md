# Google Authentication Plugin for Hippo CMS 11

This plugin allows users to login Hippo CMS by their Google email accounts.

![Login screenshot](https://github.com/canhnt/hippo-plugin-google-auth/blob/gh-pages/google-auth-login.png?raw=true)

Admins can choose which users can login via Google accounts

![Security configuration](https://github.com/canhnt/hippo-plugin-google-auth/blob/gh-pages/google-auth-config.png?raw=true)

## Requirements
* Hippo CMS from [version 11.0.2](https://www.onehippo.org/about/release-notes/11/11.0.2-release-notes.html) 
  * CMS modules: from version 4.0.3
  * Repository modules: from 4.0.3

## Configuration
### Login page
Add/modify values to the configuration path at `/hippo:configuration/hippo:frontend/login/login/loginPage`
* Property: `plugin.class`
  * Type: `String`
  * Value: `org.onehippo.forge.googleauth.cms.GoogleLoginPlugin`
* Property: `google.signin.clientid`
  * Type: `String`
  * Value: `your google app client-id`
* Property: `google.signin.scope`
  * Type: `String`
  * Value: `profile`

### Security provider
Create a new security provider at `/hippo:configuration/hippo:security/`
```
+ google-signin (hipposys:securityprovider)
  - hipposys:classname (String): org.onehippo.forge.googleauth.repository.GoogleSignInSecurityProvider
```



