//package com.optimove.sdk.optimove_sdk.main.events.decorators;
//
//import com.optimove.sdk.optimove_sdk.main.events.OptimoveEvent;
//import com.optimove.sdk.optimove_sdk.main.sdk_configs.reused_configs.EventConfigs;
//
//public final class OptimoveEventDecoratorFactory {
//
//  public static OptimoveEventDecorator newInstance(OptimoveEvent event) {
//    if (event instanceof OptimoveCoreEvent) {
//      return new OptimoveEventDecorator(event);
//    } else {
//      return new OptimoveCustomEventDecorator(event);
//    }
//  }
//
//  public static OptimoveEventDecorator newInstance(OptimoveEvent event, EventConfigs eventConfig) {
//    OptimoveEventDecorator decorator = newInstance(event);
//    decorator.processEventConfigurations(eventConfig);
//    return decorator;
//  }
//}
