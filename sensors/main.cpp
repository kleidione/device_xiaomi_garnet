/*
 * Copyright (C) 2024 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "SensorNotifier"

#include <android-base/logging.h>
#include <android-base/properties.h>

#include "notifiers/NonUiNotifier.h"

int main() {
    sp<ISensorManager> manager = ISensorManager::getService();
    if (manager == nullptr) {
        LOG(ERROR) << "failed to get ISensorManager";
        return EXIT_FAILURE;
    }

    std::vector<std::unique_ptr<SensorNotifier>> notifiers;
    notifiers.push_back(std::make_unique<NonUiNotifier>(manager));
    for (const auto& notifier : notifiers) {
        notifier->activate();
    }

    while (true) {
        // Sleep to keep the notifiers alive
        std::this_thread::sleep_for(std::chrono::seconds(10));
    }

    // Should never reach this
    return EXIT_SUCCESS;
}
