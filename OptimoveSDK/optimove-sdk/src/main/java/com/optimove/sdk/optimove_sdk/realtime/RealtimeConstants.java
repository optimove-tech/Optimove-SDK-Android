package com.optimove.sdk.optimove_sdk.realtime;

public interface RealtimeConstants {

  String REALTIME_SP_NAME = "com.optimove.sdk.realtime_shared_pref";
  /**
   * In seconds
   */
  String FIRST_VISIT_TIMESTAMP_KEY = "first_visit_timestamp";
  String DID_FAIL_SET_USER_ID_KEY = "did_fail_set_user_id";
  String DID_FAIL_SET_EMAIL_KEY = "did_fail_set_email";

  String REPORT_EVENT_REQUEST_ROUTE = "reportEvent";

  String EVENT_REQUEST_TID_KEY = "tid";
  String EVENT_REQUEST_CID_KEY = "cid";
  String EVENT_REQUEST_VID_KEY = "visitorId";
  String EVENT_REQUEST_EID_KEY = "eid";
  String EVENT_REQUEST_FIRST_VISITOR_DATE_KEY = "firstVisitorDate";
  String EVENT_REQUEST_CONTEXT_KEY = "context";

  String EVENT_RESPONSE_SUCCESS_KEY = "IsSuccess";
  String EVENT_RESPONSE_DATA_KEY = "Data";
}
