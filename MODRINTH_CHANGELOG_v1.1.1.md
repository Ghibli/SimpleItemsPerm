# 🚨 HOTFIX v1.1.1 - CRITICAL BUGFIX

**Release Date:** November 23, 2025

## ⚠️ CRITICAL BUG FIXED - UPDATE IMMEDIATELY

This hotfix resolves a major issue where the permission lore was visible even for players who had the required permission.

---

## 🐛 Bug Fixed

**Dynamic Lore System Not Working**

- ✅ **FIXED:** Lore "✘ You don't have permission" now correctly hides for players with permission
- ✅ **FIXED:** Lore comparison now works regardless of color code changes  
- ✅ **FIXED:** Compatible with items created in v1.0.0 and v1.1.0
- ✅ **FIXED:** Works even if you change the lore message in language files

---

## 🔧 Technical Changes

**Improved Lore Comparison:**
- Added `stripColorCodes()` method for reliable text comparison
- Removes ALL color codes (legacy `&`, hex `&#RRGGBB`, Minecraft native `§`)
- Compares only plain text, ignoring formatting differences

**Why This Was Needed:**
The old comparison used `line.equals(loreText)` which required an EXACT match including color codes. If the lore message changed in the language file after creating an item, the comparison would fail. Now we strip all formatting and compare only the plain text.

---

## 📋 Update Instructions

**1.** Stop your server  
**2.** Replace `SimpleItemsPerms-1.1.0.jar` with `SimpleItemsPerms-1.1.1.jar`  
**3.** Start your server  
**4.** Run `/sip reload` (optional)  
**5.** Players should relog or open inventory to update existing items

**For existing items:**
- Players just need to **relog** or **open inventory**
- Alternatively, use `/sip remove` and reassign with `/sip gui`

---

## ⚠️ Important Notes

- ✅ All permissions continue to work - this was purely a visual bug
- ✅ No configuration changes needed
- ✅ Backwards compatible with v1.0.0 and v1.1.0 items
- ✅ The `hide-for-permitted-players` config now works correctly

---

## 🎯 Compatibility

- ✅ Minecraft: 1.21, 1.21.1, 1.21.3+
- ✅ Server: Spigot, Paper, Purpur, Pufferfish
- ✅ Java: 21+

---

**Thank you for using SimpleItemsPerms!** ❤️

*If you find this update helpful, please leave a review!* ⭐
