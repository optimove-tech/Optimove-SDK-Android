# Initial Visitor ID
This field is held by the Main SDK module and represents the very first visitorId assigned by the SDK to the end user.

## Process
1. Client calls the `configure` function to start the SDK
2. SDK creates a new `UserInfo` object that is either loaded from disk or starts anew with `userId=null` and `visitorId=new UUID().substring(0,15)` 
3. The SDK tries to load the `initialVisitorId` from disk. If the `initialVisitorId` was not stored before (i.e. this is the very first run of the SDK) set it to the current `visitorId`
    
### OptiTrack Module
When calculating the **Matomo Plugins Flags** (e.g. java, flash), use the `initialVisitorId` not the `visitorId`

# Set user ID
Happens each time the client calls `Optimove.setUserId`

## Process

### Main SDK Module
1. Validate the new `userId` (illegal values were defined in the v1.0 SRS)
2. Get the current (soon to be old) `visitorId`
3. Get the updated `visitorId` as the output of the first 16 characters of `SHA1(userId)`
4. Create a `SetUserIdEvent` object from the new `userId`, `updatedVisitorId` and the `originalVisitorId`
4. Send the `setUserIdEvent` to the _OptiTrack_ Module
5. Send the `setUserIdEvent` to the _Real-Time_ Module
5. Send the new `userId` and the `initialVisitorId` to the _OptiPush_ Module
6. If the `userId` is different than the current `userId` than:
   * Override the `userId` with the new value.
   * Override the `visitorId` with the `updatedVisitorId` field

### OptiTrack Module
1. Update Matomo tracker's `userId` with the new `userId`
2. Update Matomo tracker's `visitorId` with the `updatedVisitorId`
3. Send a `setUserId` event with the 3 IDs

### Real-Time Module
1. Create a new `RealtimeEvent` for the `setUserId` event with the new `userId`, `updatedVisitorId` and the `originalVisitorId`
2. Send the event to the `RealtimeDispatcher`
3. If the request failed, raise a `DID_FAIL_SET_USER_ID` flag, store the `OriginalVisitorId` that failed
(it is already lost on the global scope since the global `UserInfo` is the first to be updated), and retry on the next `reportEvent` call / SDK startup.

### OptiPush Module
1. Create a `registerCustomer` request flagged as **conversion**
2. Set the `originalVisitorId` field of the request to receive `initialVisitorId`
