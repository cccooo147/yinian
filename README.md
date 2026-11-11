# 一念 · Yinian

一本只存在你手机里的想法手记。用 Kotlin + Jetpack Compose 写的本地 App，**没有网络权限，数据不出设备**。

> 当前版本：**3.8.6**（versionCode 21）· Android 8.0+（minSdk 26）

<p align="center">
  <img src="docs/screenshots/showcase.webp" width="100%" alt="一念：首页、卡片列表、分组底栏" />
</p>

## 它做什么

把「想法」拆成一小步一小步：每张卡片是一个还没做的事，卡片里的小步是它的推进状态。

- **分组**：底栏切换分组，也可以左右滑动翻组；点 `+` 新建，长按分组可改名 / 删除
- **每组独立**：头图、标题各自设置，新组默认没有头图
- **想法卡片**：点击进入详情；长按弹出置顶 / 换组 / 编辑 / 删除
- **小步**：点开看详情，长按编辑；状态支持 还在考虑中 / 正在做 / 等待机会 / 已实现 / 失败，也可以自设状态；可挂一条启动链接（视频、歌曲、网页），点「打开」直接跳转
- **外观**：六套纸色、浅色 / 深色 / 跟随系统

## 安装

从 [Releases](../../releases) 下载 APK，允许「安装未知应用」即可。仓库里不含签名密钥，想自己出包需要自备 `keystore.properties` + `keystore/`。

## 数据放在哪

| 内容 | 位置 |
|---|---|
| 想法、分组、外观 | `SharedPreferences`（`ideas_json` / `groups_json` / `appearance_json`） |
| 图片 | App 私有目录 `files/media`，导入时压缩 |

**没有云同步，也没有导出功能**：卸载、清除数据或换机都会丢，卸载前请自行备份。

## 构建

需要 JDK 17 与 Android SDK 35。

```powershell
# Windows（脚本会自己设置 JDK/SDK/GRADLE_USER_HOME）
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/gradle.ps1 -Task verify
```

```bash
# 其他平台
./gradlew assembleRelease
```

`verify` 会跑单元测试、lint、release 构建、签名与 zipalign 校验。

数据格式、发布流程和踩过的坑都记在 [docs/维护笔记.md](docs/维护笔记.md)。

## 已知限制

- 没有真机自动化验收，滑动手感、键盘遮挡、字体缩放都靠人工试用
- 小步配图不做裁切；封面与头图走同一个裁切器
- 没有导出 / 导入、没有自动备份
- 界面文案只有中文，未做国际化

## 许可

[MIT](LICENSE)。
