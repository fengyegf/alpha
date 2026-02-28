# Alpha

Alpha 是一款基于 Jetpack Compose 开发的 Android 多媒体解析与管理工具，支持通过可配置 API 对链接进行解析，并在应用内进行结果浏览与本地下载管理。

## 界面预览

![全部界面](https://wp-cdn.4ce.cn/v2/5HbwumM.jpeg)

## 功能特性

- 多类型解析：支持视频、音频、图集等解析类型。
- API 可配置：支持手动添加/编辑/删除解析源（名称、图标、接口地址、请求参数、字段映射）。
- 订阅导入：支持通过订阅链接批量导入解析源（当前实现以 JSON 订阅源为主）。
- 结果管理：保存本地解析历史，支持搜索与删除单条记录。
- 下载管理：解析成功后可下载媒体并在“管理”页查看/播放本地文件。
- 分类浏览：按媒体类型读取本地媒体库并分类展示。
- 在线更新提示：启动时检查远程更新信息并展示更新入口。

## 技术栈

- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 导航：Navigation Compose
- 图片加载：Coil
- 媒体播放：AndroidX Media3 (ExoPlayer)
- 本地存储：DataStore Preferences
- 网络请求：OkHttp
- JSON：Gson / org.json

## 环境要求

- Android Studio（建议最新稳定版）
- JDK 11
- Android SDK：
	- `minSdk = 29`
	- `targetSdk = 36`
	- `compileSdk = 36`

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/fengyegf/alpha.git
cd alpha
```

### 2. 使用 Android Studio 打开

- 选择项目根目录并等待 Gradle 同步完成。

### 3. 运行调试

- 连接真机或启动模拟器。
- 点击 `Run 'app'`。

## 使用说明

### 首页解析

1. 点击首页右下角“解析”按钮。
2. 输入要解析的链接并选择解析类型（如视频/音频/图集）。
3. 执行解析后，结果会进入“最近解析”列表。
4. 可在详情中复制直链、下载资源或删除记录。

### 解析源配置

1. 首页点击头像进入配置面板。
2. 在“解析源”中可：
	 - 输入订阅链接后导入解析源。
	 - 手动新增/编辑 API（接口地址、超时、类型、映射等）。
3. 若首页提示“请先添加解析源”，请先完成对应类型的 API 配置。

### 搜索与管理

- 搜索页：支持按标题/作者/描述检索本地解析历史。
- 管理页：展示已下载任务，支持调用系统播放器打开本地文件。

## 订阅源与接口说明

### 订阅导入

- 应用会从订阅 URL 拉取内容并解析为解析源列表（当前主要支持 JSON）。
- 每个源通常包含：`name`、`icon`、`url`、`type`、`time`、`Query`、`response` 等字段。

### API URL 占位

- 解析接口地址通常使用 `{url}` 占位：
	- 示例：`https://example.com/api?url={url}`

### 响应字段映射

- 支持通过映射提取字段（例如标题、作者、封面、直链等）。
- 常见占位包括：`${title}`、`${author}`、`${cover}`、`${videoUrl}`、`${imageUrls}`。

## 项目结构（核心）

```text
app/src/main/java/com/appecho/alpha/
├─ MainActivity.kt                    # 应用入口、底部导航
├─ logic/
│  ├─ ApiAnalyzer.kt                  # 解析请求与响应提取
│  └─ SubscriptionHelper.kt           # 订阅拉取与导入
└─ ui/theme/
	 ├─ home/                           # 首页、解析与详情
	 ├─ search/                         # 搜索页
	 ├─ category/                       # 分类页
	 ├─ manage/                         # 下载管理页
	 └─ ProfileListItem/                # API 配置页
```

## 常见问题

- 解析失败：请检查链接是否有效、API 是否可用、映射是否正确。
- 无法下载：请确认直链可访问，或尝试更换解析源。
- 导入失败：请确认订阅地址可访问且返回格式符合预期。

## 免责声明

- 本项目仅用于技术学习与研究，请遵守当地法律法规及平台服务条款。
- 请勿将本项目用于侵权、非法传播或其他违法用途。

## ⚠️ 开源许可声明 (Important)

本项目的代码遵循 **[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh)** 许可协议。

- **非商业性使用 (Non-Commercial)**：您不得将本软件及其代码用于任何形式的商业盈利目的（包括但不限于付费下载、内置广告、售卖源码）。
- **相同方式共享 (ShareAlike)**：如果您对代码进行了修改或二次开发，必须以相同的许可协议开源您的衍生版本。
- **署名 (Attribution)**：必须保留原作者的版权声明及项目链接。

**严禁任何人利用本项目进行非法牟利。如有发现，作者保留追究法律责任的权利。**
