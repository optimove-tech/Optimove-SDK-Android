package com.optimove.sdk.optimove_sdk.fixtures;

import com.google.gson.Gson;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.FetchedLocalConfigsMapper;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.configs.Configs;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedGlobalConfig;
import com.optimove.sdk.optimove_sdk.main.sdk_configs.fetched_configs.FetchedTenantConfigs;

public class ConfigProvider {

    public static Configs getConfigs(){
        Gson gson = new Gson();
        FetchedTenantConfigs fetchedTenantConfigs = gson.fromJson(ConfigProvider.getTenantConfigJsonString(),
                FetchedTenantConfigs.class);
        FetchedGlobalConfig fetchedGlobalConfig = gson.fromJson(ConfigProvider.getGlobalConfigJsonString(),
                FetchedGlobalConfig.class);
        return FetchedLocalConfigsMapper.mapFetchedConfigsToLocal(
                fetchedGlobalConfig, fetchedTenantConfigs);
    }

    public static String getGlobalConfigJsonString() {

        return "{\n" +
                "    \"general\": {\n" +
                "        \"logs_service_endpoint\": \"https://us-central1-mobilepush-161510.cloudfunctions.net/reportLog\"\n" +
                "    },\n" +
                "    \"optitrack\": {\n" +
                "        \"event_category_name\": \"LogEvent\",\n" +
                "        \"custom_dimension_ids\": {\n" +
                "            \"event_id_custom_dimension_id\": 6,\n" +
                "            \"event_name_custom_dimension_id\": 7,\n" +
                "            \"visit_custom_dimensions_start_id\": 1,\n" +
                "            \"max_visit_custom_dimensions\": 5,\n" +
                "            \"action_custom_dimensions_start_id\": 8,\n" +
                "            \"max_action_custom_dimensions\": 25\n" +
                "        }\n" +
                "    },\n" +
                "    \"optipush\": {\n" +
                "        \"mbaas_endpoint\": \"https://registartion-service-prod.optimove.net/\"\n" +
                "    },\n" +
                "    \"core_events\": {\n" +
                "        \"set_user_id_event\": {\n" +
                "            \"id\": 1001,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"originalVisitorId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"userId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"updatedVisitorId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"set_email_event\": {\n" +
                "            \"id\": 1002,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"email\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"user_agent_header_event\": {\n" +
                "            \"id\": 1005,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"user_agent_header1\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"user_agent_header2\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"set_page_visit\": {\n" +
                "            \"id\": 1006,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"customURL\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"pageTitle\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"category\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"set_advertising_id\": {\n" +
                "            \"id\": 1010,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"advertising_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_delivered\": {\n" +
                "            \"id\": 1012,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"campaign_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"engagement_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"campaign_type\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 17\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 18\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_opened\": {\n" +
                "            \"id\": 1013,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"campaign_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"engagement_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"campaign_type\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 17\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 18\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_dismissed\": {\n" +
                "            \"id\": 1014,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"campaign_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"engagement_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"campaign_type\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 17\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 18\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"optipush_opt_in\": {\n" +
                "            \"id\": 1017,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"optipush_opt_out\": {\n" +
                "            \"id\": 1018,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_ping\": {\n" +
                "            \"id\": 1019,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"user_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"visitor_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"app_open\": {\n" +
                "            \"id\": 1020,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"user_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"visitor_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"optimove_sdk_metadata\": {\n" +
                "            \"id\": 1007,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"sdk_platform\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"sdk_version\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"config_file_url\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"triggered_notification_received\": {\n" +
                "            \"id\": 1021,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"action_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"triggered_notification_opened\": {\n" +
                "            \"id\": 1022,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"action_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

    }
    public static String getTenantConfigJsonString() {
        return "{\n" +
                "    \"version\": \"1.2.0\",\n" +
                "    \"enableOptitrack\": true,\n" +
                "    \"enableOptipush\": true,\n" +
                "    \"enableVisitors\": true,\n" +
                "    \"enableRealtime\": true,\n" +
                "    \"realtimeMetaData\": {\n" +
                "        \"realtimeToken\": \"054454057e5b6b5cb85a7d06f22e929fd8f32682b5f81b29deb6dd778e7e5184\",\n" +
                "        \"realtimeGateway\": \"http://173.255.119.3/\",\n" +
                "        \"options\": {\n" +
                "            \"showDimmer\": true,\n" +
                "            \"showWatermark\": true,\n" +
                "            \"popupCallback\": null\n" +
                "        }\n" +
                "    },\n" +
                "    \"optitrackMetaData\": {\n" +
                "        \"sendUserAgentHeader\": true,\n" +
                "        \"enableHeartBeatTimer\": false,\n" +
                "        \"heartBeatTimer\": 30,\n" +
                "        \"eventCategoryName\": \"LogEvent\",\n" +
                "        \"eventIdCustomDimensionId\": 6,\n" +
                "        \"eventNameCustomDimensionId\": 7,\n" +
                "        \"visitCustomDimensionsStartId\": 1,\n" +
                "        \"maxVisitCustomDimensions\": 5,\n" +
                "        \"actionCustomDimensionsStartId\": 8,\n" +
                "        \"maxActionCustomDimensions\": 25,\n" +
                "        \"optitrackEndpoint\": \"http://104.197.238.220/\",\n" +
                "        \"siteId\": 107\n" +
                "    },\n" +
                "    \"mobile\": {\n" +
                "        \"optipushMetaData\": {\n" +
                "            \"enableAdvertisingIdReport\": true,\n" +
                "            \"otherRegistrationServiceEndpoint\": \"https://registartion-service-prod.optimove.net/\",\n" +
                "            \"onlyRegistrationServiceEndpoint\": \"https://registartion-service-prod.optimove.net/\",\n" +
                "            \"pushTopicsRegistrationEndpoint\": \"https://us-central1-appcontrollerproject-developer.cloudfunctions.net/\"\n" +
                "        },\n" +
                "        \"firebaseProjectKeys\": {\n" +
                "            \"appIds\": {\n" +
                "                \"android\": {\n" +
                "                    \"com.optimove.sdk.optimovemobilesdk\": \"1:668592491240:android:d4bac06800e0e77a\",\n" +
                "                    \"com.optimove.sdk.unity\": \"1:668592491240:android:306607c127b690b5\"\n" +
                "                },\n" +
                "                \"ios\": {\n" +
                "                    \"com.optimove.sdk.dev.host\": \"1:668592491240:ios:58b2f15561e218db\",\n" +
                "                    \"com.optimove.sdk.demo.swift\": \"1:668592491240:ios:d2b42ad788bf7404\",\n" +
                "                    \"com.optimove.sdk.demo.objc\": \"1:668592491240:ios:de792a08568beb9f\",\n" +
                "                    \"com.optimove.sdk.unity\": \"1:668592491240:ios:306607c127b690b5\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"webApiKey\": \"AIzaSyAbe-FyTij_t_P2lX8RycHKbMqS3Fmm-kA\",\n" +
                "            \"dbUrl\": \"appcontrollerproject-developer.firebaseio.com\",\n" +
                "            \"senderId\": \"668592491240\",\n" +
                "            \"storageBucket\": \"appcontrollerproject-developer.appspot.com\",\n" +
                "            \"projectId\": \"appcontrollerproject-developer\"\n" +
                "        },\n" +
                "        \"clientsServiceProjectKeys\": {\n" +
                "            \"appIds\": {\n" +
                "                \"android\": {\n" +
                "                    \"android.master.app\": \"1:871123552739:android:2c7f6d5c1cc30459\"\n" +
                "                },\n" +
                "                \"ios\": {\n" +
                "                    \"ios.master.app\": \"1:871123552739:ios:c6eb1455c1b8d767\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"webApiKey\": \"AIzaSyBAZgQBxZNMeQF62ig9bsvWhxFLGqlnEBk\",\n" +
                "            \"dbUrl\": \"https://sdkclientsservice.firebaseio.com/\",\n" +
                "            \"senderId\": \"871123552739\",\n" +
                "            \"storageBucket\": \"sdkclientsservice.appspot.com\",\n" +
                "            \"projectId\": \"sdkclientsservice\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"events\": {\n" +
                "        \"set_user_id_event\": {\n" +
                "            \"id\": 1001,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"originalVisitorId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"originalVisitorId\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"userId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"userId\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"updatedVisitorId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"updatedVisitorId\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"set_email_event\": {\n" +
                "            \"id\": 1002,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"email\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"email\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"page_category_event\": {\n" +
                "            \"id\": 1003,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"category\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"category\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"stitch_event\": {\n" +
                "            \"id\": 1004,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"sourcePublicCustomerId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"sourcePublicCustomerId\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"sourceVisitorId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"sourceVisitorId\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"targetVsitorId\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"targetVisitorId\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"user_agent_header_event\": {\n" +
                "            \"id\": 1005,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"user_agent_header1\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"user_agent_header1\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"user_agent_header2\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"user_agent_header2\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"set_page_visit\": {\n" +
                "            \"id\": 1006,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"customURL\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"customURL\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"pageTitle\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"pageTitle\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"category\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"category\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"set_advertising_id\": {\n" +
                "            \"id\": 1010,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"advertising_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"advertising_id\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"device_id\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_delivered\": {\n" +
                "            \"id\": 1012,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"timestamp\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"campaign_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"campaign_id\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"action_serial\",\n" +
                "                    \"id\": 4,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"template_id\",\n" +
                "                    \"id\": 5,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"engagement_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"engagement_id\",\n" +
                "                    \"id\": 6,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"campaign_type\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"campaign_type\",\n" +
                "                    \"id\": 7,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 17\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 18\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_opened\": {\n" +
                "            \"id\": 1013,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"timestamp\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"campaign_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"campaign_id\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"action_serial\",\n" +
                "                    \"id\": 4,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"template_id\",\n" +
                "                    \"id\": 5,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"engagement_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"engagement_id\",\n" +
                "                    \"id\": 6,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"campaign_type\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"campaign_type\",\n" +
                "                    \"id\": 7,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 17\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 18\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_dismissed\": {\n" +
                "            \"id\": 1014,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"timestamp\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"campaign_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"campaign_id\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"action_serial\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"action_serial\",\n" +
                "                    \"id\": 4,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"template_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"template_id\",\n" +
                "                    \"id\": 5,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"engagement_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"engagement_id\",\n" +
                "                    \"id\": 6,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"campaign_type\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"campaign_type\",\n" +
                "                    \"id\": 7,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 16\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 17\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 18\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"optipush_opt_in\": {\n" +
                "            \"id\": 1017,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"timestamp\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"device_id\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"optipush_opt_out\": {\n" +
                "            \"id\": 1018,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"timestamp\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"timestamp\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"device_id\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"notification_ping\": {\n" +
                "            \"id\": 1019,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"device_id\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"user_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"user_id\",\n" +
                "                    \"id\": 4,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"visitor_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"visitor_id\",\n" +
                "                    \"id\": 5,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"app_open\": {\n" +
                "            \"id\": 1020,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"device_id\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"device_id\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"user_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"user_id\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"visitor_id\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"visitor_id\",\n" +
                "                    \"id\": 4,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"optimove_sdk_metadata\": {\n" +
                "            \"id\": 1021,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": false,\n" +
                "            \"parameters\": {\n" +
                "                \"sdk_platform\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"sdk_platform\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"sdk_version\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"sdk_version\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"config_file_url\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"config_file_url\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"app_ns\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"app_ns\",\n" +
                "                    \"id\": 4,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 15\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"simple_custom_event\": {\n" +
                "            \"id\": 4333,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"string_param\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"string_param\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"number_param\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"number_param\",\n" +
                "                    \"id\": 2,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"boolean_param\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"boolean_param\",\n" +
                "                    \"id\": 3,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"realtime_test_event\": {\n" +
                "            \"id\": 1102,\n" +
                "            \"supportedOnOptitrack\": false,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"deposit\": {\n" +
                "                    \"optional\": false,\n" +
                "                    \"name\": \"deposit\",\n" +
                "                    \"id\": 1,\n" +
                "                    \"type\": \"Number\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 12\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 13\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 14\n" +
                "                }\n" +
                "            }\n" +
                "        },\n" +
                "        \"event_no_params\": {\n" +
                "            \"id\": 1102,\n" +
                "            \"supportedOnOptitrack\": true,\n" +
                "            \"supportedOnRealTime\": true,\n" +
                "            \"parameters\": {\n" +
                "                \"event_platform\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventPlatform\",\n" +
                "                    \"id\": 1000,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 8\n" +
                "                },\n" +
                "                \"event_device_type\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventDeviceType\",\n" +
                "                    \"id\": 1001,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 9\n" +
                "                },\n" +
                "                \"event_os\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventOS\",\n" +
                "                    \"id\": 1002,\n" +
                "                    \"type\": \"String\",\n" +
                "                    \"optiTrackDimensionId\": 10\n" +
                "                },\n" +
                "                \"event_native_mobile\": {\n" +
                "                    \"optional\": true,\n" +
                "                    \"name\": \"EventNativeMobile\",\n" +
                "                    \"id\": 1003,\n" +
                "                    \"type\": \"Boolean\",\n" +
                "                    \"optiTrackDimensionId\": 11\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
    }




}
