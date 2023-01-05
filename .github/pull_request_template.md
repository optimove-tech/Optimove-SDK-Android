### Description of Changes

(briefly outline the reason for changes, and describe what's been done)

### Breaking Changes

- None

### Release Checklist

Prepare:

- [ ] Detail any breaking changes. Breaking changes require a new major version number, and a migration guide in wiki / README.md

Bump versions in:

- [ ] CHANGELOG.md
- [ ] gradle.properties

### Integration tests

_T&T Only_

- [ ] Init SDK with only optimove credentials
- [ ] Associate customer
- [ ] Associate email
- [ ] Track events

_Mobile Only_

- [ ] Init SDK with all credentials
- [ ] Track events
- [ ] Associate customer (verify both backends)
- [ ] Register for push
- [ ] Opt-in for In-App
- [ ] Send test push
- [ ] Send test In-App
- [ ] Receive / trigger deep link handler (In-App/Push)
- [ ] Receive / trigger the content extension, render image and action buttons for push
- [ ] Verify push opened handler

_Deferred Deep Links_

- [ ] With app installed, trigger deep link handler
- [ ] With app uninstalled, follow deep link, install test bundle, verify deep link read from Clipboard, trigger deep link handler

_Combined_

- [ ] Track event for T&T, verify push received
- [ ] Trigger scheduled campaign, verify push received
- [ ] Trigger scheduled campaign, verify In-App received

### Release Procedure

- [ ] Squash and merge `dev` to `master`
- [ ] Delete branch once merged
