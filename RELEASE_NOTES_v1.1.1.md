# SimpleItemsPerms v1.1.1 - HOTFIX CRITICO

**Release Date:** November 23, 2025

## 🚨 CRITICAL BUGFIX

This is a **critical hotfix** that resolves a major issue with the dynamic lore system.

---

## 🐛 Bug Fixed

**Dynamic Lore Not Working**
- ✅ **FIXED:** The permission lore ("✘ You don't have permission to use this") now correctly hides for players who HAVE the permission
- ✅ **FIXED:** Lore comparison now works regardless of color formatting changes
- ✅ **FIXED:** Works with items created with previous plugin versions

---

## 🔧 Technical Changes

### Improved Lore Comparison System
- Added `stripColorCodes()` method that removes ALL color codes before comparison
- Now compares plain text instead of exact formatted strings
- Supports:
  - Legacy codes (`&[0-9a-fk-or]`)
  - Hex codes (`&#RRGGBB`)
  - Translated codes (`§[0-9a-fk-or]`)
  - Native Minecraft hex (`§x§R§R§G§G§B§B`)

### Why This Fix Was Needed
**Problem:** The lore comparison used `line.equals(loreText)` which required an EXACT match including all color codes. If you:
1. Created an item with lore
2. Changed the lore message in the language file
3. The comparison would fail because the saved lore ≠ new lore text

**Solution:** Strip all color codes and compare only the plain text. This makes the comparison reliable regardless of formatting.

---

## 📋 Update Instructions

### For Server Admins:

1. **Stop your server**
2. **Backup** your current plugin (just in case!)
3. **Replace** the old JAR with `SimpleItemsPerms-1.1.1.jar`
4. **Start** your server
5. **Run** `/sip reload` (optional, but recommended)

### For Existing Items:

**Option A - Automatic (Recommended):**
- Players just need to **relog** or **open their inventory**
- The lore will update automatically based on their permissions

**Option B - Manual Recreation:**
- Use `/sip remove` on old items
- Reassign permissions with `/sip gui`
- This guarantees 100% compatibility

---

## ⚠️ Important Notes

- **All permissions still work!** This was purely a visual bug with the lore display
- Items created with v1.0.0 or v1.1.0 will work with this fix
- The `hide-for-permitted-players` config option now works correctly
- No configuration changes needed

---

## 🎯 Compatibility

- ✅ **Minecraft:** 1.21, 1.21.1, 1.21.3+
- ✅ **Server Software:** Spigot, Paper, Purpur, Pufferfish
- ✅ **Java Version:** 21+
- ✅ **Backwards Compatible:** Items from v1.0.0 and v1.1.0 work perfectly

---

## 📊 Testing

This hotfix has been tested with:
- Items created in v1.0.0
- Items created in v1.1.0
- Players with and without permissions
- Different lore configurations (start/end position)
- Language file modifications
- Multiple permission levels

All scenarios now work correctly! ✅

---

## 🙏 Thank You

Thanks to the community for reporting this issue quickly!

If you encounter any other issues, please report them on:
- SpigotMC Discussion Page
- Modrinth Issues Tab

---

**Developed with ❤️ by AlessioGTAII**

SimpleItemsPerms v1.1.1 - © 2025
