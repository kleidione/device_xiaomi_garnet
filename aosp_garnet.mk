#
# Copyright (C) 2024 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

TARGET_BOOT_ANIMATION_RES := 1080
TARGET_ARCH := arm64
TARGET_INCLUDE_STOCK_ARCORE := true
TARGET_SUPPORTS_GOOGLE_RECORDER := true
TARGET_INCLUDE_LIVE_WALLPAPERS := true
USE_PIXEL_CHARGER := true
TARGET_SUPPORTS_CALL_RECORDING := true
TARGET_SUPPORTS_GOOGLE_BATTERY := false
TARGET_FLATTEN_APEX := false
MAINLINE_INCLUDE_VIRT_MODULE := false
TARGET_INCLUDE_CARRIER_SETTINGS := true
TARGET_SUPPORTS_QUICK_TAP := true

# Inherit some common The PixelProject stuff.
$(call inherit-product, vendor/aosp/config/common_full_phone.mk)

# Inherit from garnet device
$(call inherit-product, device/xiaomi/garnet/device.mk)

# Inherit from the MiuiCamera setup
$(call inherit-product-if-exists, vendor/xiaomi/garnet-miuicamera/products/miuicamera.mk)

PRODUCT_NAME := aosp_garnet
PRODUCT_DEVICE := garnet
PRODUCT_MANUFACTURER := Xiaomi
PRODUCT_BRAND := Redmi
PRODUCT_MODEL := 2312DRA50G

PRODUCT_SYSTEM_NAME := garnet_global
PRODUCT_SYSTEM_DEVICE := garnet

PRODUCT_BUILD_PROP_OVERRIDES += \
    PRIVATE_BUILD_DESC="garnet_global-user 13 TKQ1.221114.001 V14.0.6.0.TNRMIXM release-keys" \
    TARGET_DEVICE=$(PRODUCT_SYSTEM_DEVICE) \
    TARGET_PRODUCT=$(PRODUCT_SYSTEM_NAME)

BUILD_FINGERPRINT := Redmi/garnet_global/garnet:13/TKQ1.221114.001/V14.0.6.0.TNRMIXM:user/release-keys

PRODUCT_GMS_CLIENTID_BASE := android-xiaomi
