# ABC Corporation — Employee Hub Portal

Complete step-by-step guide to building the **ABC Employee Hub** intranet on **Adobe Experience Manager (AEM) 2025.6**.

This README walks you from **Maven archetype scaffold** through **templates, components, services, servlets, content, and deployment** in the correct order.

**Architecture reference:** see [DOCUMENTATION.md](./DOCUMENTATION.md) for templates, policies, OSGi, services, servlets, components, pages, and content structure.

**Step-by-step checklist:** see [PROGRESS.md](./PROGRESS.md) (13 phases, verify or rebuild).

---

## Table of Contents

1. [Overview](#overview)
2. [AEM Authoring Essentials](#aem-authoring-essentials)
3. [Prerequisites](#prerequisites)
4. [Phase 0 — Install AEM SDK & Maven Dependencies](#phase-0--install-aem-sdk--maven-dependencies)
5. [Phase 1 — Generate Project with Maven Archetype](#phase-1--generate-project-with-maven-archetype)
6. [Phase 2 — Customize the Archetype Output](#phase-2--customize-the-archetype-output)
7. [Phase 3 — UI Config (OSGi Foundation)](#phase-3--ui-config-osgi-foundation)
8. [Phase 4 — Core Module (Bottom-Up)](#phase-4--core-module-bottom-up)
9. [Phase 5 — Page Component & Base Clientlibs](#phase-5--page-component--base-clientlibs)
10. [Phase 6 — Structure Components](#phase-6--structure-components)
11. [Phase 7 — Editable Templates & Policies (Before Business Components)](#phase-7--editable-templates--policies-before-business-components)
12. [Phase 8 — Common Components](#phase-8--common-components)
13. [Phase 9 — Business Components (With Models & Dialogs)](#phase-9--business-components-with-models--dialogs)
14. [Phase 10 — Feature Clientlibs (Search & FAQ JS)](#phase-10--feature-clientlibs-search--faq-js)
15. [Phase 11 — Content, Tags & Data in AEM](#phase-11--content-tags--data-in-aem)
16. [Phase 12 — All Module & Maven Deploy Profile](#phase-12--all-module--maven-deploy-profile)
17. [Deployment Commands](#deployment-commands)
18. [Demo Flow & Test URLs](#demo-flow--test-urls)
19. [Reference — Components, Services, Servlets](#reference--components-services-servlets)
20. [Troubleshooting](#troubleshooting)

---

## Overview

### What this project delivers

| Feature | Implementation |
|---------|---------------|
| Employee directory & live search | `EmployeeSearch` component + `EmployeeSearchServlet` + `EmployeeService` |
| Department browser | `DepartmentCard` component + `DepartmentSearchServlet` + `DepartmentService` |
| FAQ accordion | `FAQ` component (multifield dialog) + `FAQModel` |
| Announcement banner | `Announcement` component + `AnnouncementService` (OSGi config) |
| Testimonials | `Testimonial` component (multifield dialog) |
| Landing & content pages | 2 editable templates with policies |

### Recommended build order (summary)

```
Archetype → ui.config → core (config → service → model → servlet)
         → page + base clientlibs
         → structure components (header, footer, hero…)
         → EDITABLE TEMPLATES + POLICIES   ← before business components
         → common components (title, text, image…)
         → business components + dialogs
         → feature clientlibs (search, faq)
         → content/tags/data in AEM
         → all module + deploy
```

> **Why templates before business components?**  
> Template **policies** define which components authors can use. Create templates and policies first, then build the components listed in those policies. Structure components (header, hero) are referenced in the template **structure** layer and must exist before templates are deployed.

---

## AEM Authoring Essentials

These rules are required for a working page editor (Content Tree, dialogs, styles in edit mode). Full detail: [DOCUMENTATION.md](./DOCUMENTATION.md).

### Page component — extend Core WCM Page v3

| Do | Don't |
|----|--------|
| Supertype `core/wcm/components/page/v3/page` | Custom `page.html` replacing Core Page |
| `body.html` with **TemplatedContainer** + `.eh-page` wrapper | Plain `data-sly-resource` on root without TemplatedContainer |
| `customheaderlibs.html` / `customfooterlibs.html` load clientlibs in **all modes** | Block CSS/JS when `wcmmode=edit` |
| Scope CSS to `.eh-page` | Global `* { margin:0; padding:0 }` |

### Template policies

- `templates/<name>/policies/` must be **`cq:Page`** (design JSON must return HTTP **200**).
- Add `template-types/page` under `/conf/employeehub/settings/wcm/`.
- Set `cq:templateType` on each template.
- Template **structure** layer → `sling:resourceType="employeehub/components/page"`.

### Site + conf

Set `cq:conf="/conf/employeehub"` on `/content/employeehub` (in ui.content).

### `_cq_editConfig`

Use string `cq:actions` — not nested `<edit/>` nodes:

```xml
<jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="nt:unstructured"
    cq:actions="[edit,delete,copymove,insert]"
    cq:dialogMode="floating"
    cq:layout="editbar"/>
```

Every component with a dialog needs **`_cq_dialog`** and **`_cq_editConfig`**.

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| AEM Author SDK | 2025.6 | Start JAR → `localhost:4502` |
| JDK | 11 | `java -version` |
| Maven | 3.8.5+ (3.3.9+ recommended) | `mvn -version` |
| IDE | IntelliJ / VS Code | Optional |

**Default AEM login:** `admin` / `admin`

---

## Phase 0 — Install AEM SDK & Maven Dependencies

### 0.1 Start AEM Author

```bash
# Unzip AEM SDK and run the Author jar
java -jar aem-sdk-quickstart-2025.6.xxx-author-4502.jar
```

Verify: http://localhost:4502 → login page appears.

### 0.2 Install AEM SDK API into local Maven repo

The `aem-sdk-api` JAR is **not** in Maven Central. Extract it from the SDK zip:

```bash
# Find aem-sdk-api-*.jar inside the SDK zip, then:
mvn install:install-file \
  -Dfile=aem-sdk-api-2025.6.21193.20250609T124356Z.jar \
  -DgroupId=com.adobe.aem \
  -DartifactId=aem-sdk-api \
  -Dversion=2025.6.21193.20250609T124356Z \
  -Dpackaging=jar
```

> Use the exact version string from your SDK JAR filename. Update `aem.sdk.api` in the parent `pom.xml` to match.

---

## Phase 1 — Generate Project with Maven Archetype

Adobe provides the **AEM Project Archetype** to scaffold a best-practices multi-module project.

### 1.1 Run the archetype

From any empty directory:

```bash
mvn -B org.apache.maven.plugins:maven-archetype-plugin:3.3.1:generate \
  -DarchetypeGroupId=com.adobe.aem \
  -DarchetypeArtifactId=aem-project-archetype \
  -DarchetypeVersion=56 \
  -DappTitle="ABC Employee Hub" \
  -DappId="employeehub" \
  -DgroupId="com.abc.employeehub" \
  -DaemVersion=cloud \
  -DsdkVersion=2025.6.21193.20250609T124356Z \
  -DincludeForms=n \
  -DincludeCommunities=n \
  -DincludeErrorHandler=n \
  -DfrontendModule=none \
  -Dlanguage=java \
  -DsingleCountry=y \
  -Dcountry=us \
  -Ddatalayer=y
```

### 1.2 Archetype properties explained

| Property | Value | Purpose |
|----------|-------|---------|
| `appTitle` | ABC Employee Hub | Human-readable project name |
| `appId` | employeehub | Folder name: `/apps/employeehub`, `/content/employeehub` |
| `groupId` | com.abc.employeehub | Maven groupId |
| `aemVersion` | cloud | AEM as a Cloud Service SDK (also works for local SDK) |
| `sdkVersion` | 2025.6.xxx | Must match your installed SDK API version |
| `frontendModule` | none | We use clientlibs instead of Webpack/Frontend module |
| `language` | java | HTL + Java Sling Models (not SPA) |

### 1.3 What the archetype creates

```
employeehub/
├── pom.xml              # Parent POM
├── core/                # OSGi bundle
├── ui.apps/             # /apps/employeehub — components, clientlibs
├── ui.content/          # /content/employeehub — seed content
├── ui.config/           # OSGi configs + editable templates (/conf/employeehub)
├── ui.apps.structure/   # Repo structure (optional — can remove)
├── all/                 # Container package
└── dispatcher/          # Optional — not needed for local Author demo
```

### 1.4 First build (verify scaffold)

```bash
cd employeehub
mvn clean install
```

Expected: `BUILD SUCCESS` — packages built locally, nothing deployed yet.

---

## Phase 2 — Customize the Archetype Output

### 2.1 Set AEM connection properties (parent `pom.xml`)

```xml
<aem.host>localhost</aem.host>
<aem.port>4502</aem.port>
<aem.user>admin</aem.user>
<aem.password>admin</aem.password>
<aem.sdk.api>2025.6.21193.20250609T124356Z</aem.sdk.api>
```

### 2.2 Configure ui.apps filter

**File:** `ui.apps/src/main/content/META-INF/vault/filter.xml`

Use **narrow filters** — do not claim all of `/apps/employeehub` or ui.apps redeploys will remove `osgiconfig` owned by ui.config.

```xml
<workspaceFilter version="1.0">
    <filter root="/apps/employeehub/components"/>
    <filter root="/apps/employeehub/clientlibs"/>
    <filter root="/apps/employeehub/install"/>
</workspaceFilter>
```

### 2.3 Configure ui.content filter (seed/reference only)

**File:** `ui.content/src/main/content/META-INF/vault/filter.xml`

```xml
<workspaceFilter version="1.0">
    <filter root="/content/employeehub"/>
    <filter root="/content/dam/employeehub"/>
    <filter root="/content/cq:tags/employeehub"/>
</workspaceFilter>
```

### 2.4 Configure ui.config filter

**File:** `ui.config/src/main/content/META-INF/vault/filter.xml`

```xml
<workspaceFilter version="1.0">
    <filter root="/apps/employeehub/osgiconfig"/>
    <filter root="/conf/employeehub"/>
</workspaceFilter>
```

> **Editable templates** live in `ui.config` under `/conf/employeehub` and deploy with `-PautoInstallSinglePackage`.

### 2.5 Embed core bundle in ui.apps

**File:** `ui.apps/pom.xml` — inside `filevault-package-maven-plugin` configuration:

```xml
<embeddeds>
    <embedded>
        <groupId>com.abc.employeehub</groupId>
        <artifactId>employeehub.core</artifactId>
        <target>/apps/employeehub/install</target>
    </embedded>
</embeddeds>
```

This deploys the OSGi bundle automatically when `ui.apps` is installed.

### 2.6 Configure autoInstallSinglePackage profile (parent `pom.xml`)

Add profile so code deploys with:

```bash
mvn clean install -PautoInstallSinglePackage
```

See [Phase 12](#phase-12--all-module--maven-deploy-profile) for full profile configuration.

---

## Phase 3 — UI Config (OSGi Foundation)

> **Do this before servlets** — servlet paths and service users must exist before the core bundle starts.

**Path:** `ui.config/src/main/content/jcr_root/apps/employeehub/osgiconfig/`

```
osgiconfig/
├── config/              ← all run modes (infrastructure)
├── config.author/       ← author-only app settings
└── config.publish/      ← publish-only app settings
```

Create these files **in order**:

### 3.1 Repository Initializer (service user + ACLs)

**File:** `config/org.apache.sling.jcr.repoinit.RepositoryInitializer~employeehub.cfg.json`

```json
{
  "scripts": [
    "create service user employeehub-service",
    "set ACL for employeehub-service\n  allow jcr:read on /content/employeehub/data\n  allow jcr:read on /content/dam/employeehub\nend"
  ]
}
```

### 3.2 Service User Mapping

**File:** `config/org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended~employeehub.cfg.json`

```json
{
  "user.mapping": [
    "employeehub.core:employeehub-service=[employeehub-service]"
  ]
}
```

### 3.3 Sling Servlet Resolver (allow /bin/employeehub/)

**File:** `config/org.apache.sling.servlets.resolver.SlingServletResolver~employeehub.cfg.json`

```json
{
  "servletresolver.paths": [
    "/bin/",
    "/bin/employeehub/"
  ]
}
```

### 3.4 Employee Hub OSGi Configuration (run mode specific)

Application configs live under **`config.author/`** and **`config.publish/`** so Author and Publish can use different values. Shared infrastructure (repoinit, service user, servlet resolver) stays in **`config/`**.

**Author —** `config.author/com.abc.employeehub.core.configurations.EmployeeHubConfiguration.cfg.json`

```
portalName="ABC Employee Hub"
environmentName="author"
defaultDepartment="all"
maximumSearchResults:Integer=25
```

**Publish —** `config.publish/com.abc.employeehub.core.configurations.EmployeeHubConfiguration.cfg.json`

```
portalName="ABC Employee Hub"
environmentName="publish"
defaultDepartment="all"
maximumSearchResults:Integer=15
```

### 3.5 Announcement OSGi Configuration (run mode specific)

**Author —** `config.author/com.abc.employeehub.core.configurations.AnnouncementConfiguration.cfg.json`

```
announcementMessage="[Author Preview] Welcome to ABC Employee Hub — content authoring environment."
bannerColor="#0066cc"
enableBanner=true
```

**Publish —** `config.publish/com.abc.employeehub.core.configurations.AnnouncementConfiguration.cfg.json`

```
announcementMessage="Welcome to ABC Employee Hub! Stay connected with company updates."
bannerColor="#004499"
enableBanner=true
```

> On Author (`:4502`), only `config/` + `config.author/` apply. On Publish (`:4503`), only `config/` + `config.publish/` apply.

### 3.6 Deploy ui.config only (first deploy)

```bash
mvn clean install -pl ui.config -PautoInstallSinglePackage
```

Verify in OSGi console: http://localhost:4502/system/console/configMgr → search "Employee Hub" → confirm **author** values (`environmentName=author`, `maximumSearchResults=25`).

On Publish (if running on `:4503`), verify `environmentName=publish` and `maximumSearchResults=15`.

---

## Phase 4 — Core Module (Bottom-Up)

**Path:** `core/src/main/java/com/abc/employeehub/core/`

Build in this **exact order** — each layer depends on the previous:

```
OSGi Config interfaces  →  Services  →  Sling Models  →  Servlets
```

### 4.1 OSGi Configuration Interfaces (Metatype)

**Package:** `configurations/`

| File | OSGi PID | Key Properties |
|------|----------|---------------|
| `EmployeeHubConfiguration.java` | `com.abc.employeehub.core.configurations.EmployeeHubConfiguration` | portalName, maximumSearchResults |
| `AnnouncementConfiguration.java` | `com.abc.employeehub.core.configurations.AnnouncementConfiguration` | announcementMessage, bannerColor, enableBanner |

**Pattern:**

```java
@ObjectClassDefinition(name = "Employee Hub Configuration")
public @interface EmployeeHubConfiguration {
    @AttributeDefinition(name = "Maximum Search Results")
    int maximumSearchResults() default 20;
}
```

### 4.2 OSGi Services

**Package:** `services/` (interfaces) + `services/impl/` (implementations)

| Order | Interface | Implementation | Reads From |
|-------|-----------|---------------|------------|
| 1 | `EmployeeService` | `EmployeeServiceImpl` | `/content/employeehub/data/employees` |
| 2 | `DepartmentService` | `DepartmentServiceImpl` | `/content/employeehub/data/departments` |
| 3 | `AnnouncementService` | `AnnouncementServiceImpl` | OSGi `AnnouncementConfiguration` |
| 4 | `FAQService` | `FAQServiceImpl` | JCR path utility |

**EmployeeService key methods:**

```java
Map<String, Object> getEmployee(ResourceResolver resolver, String employeePath);
List<Map<String, Object>> searchEmployees(ResourceResolver resolver, String query, int maxResults);
Map<String, Object> getEmployeeDetails(ResourceResolver resolver, String employeeId);
```

**Service registration pattern:**

```java
@Component(service = EmployeeService.class, immediate = true)
@Designate(ocd = EmployeeHubConfiguration.class)
public class EmployeeServiceImpl implements EmployeeService { ... }
```

### 4.3 Sling Models

**Package:** `models/`

Create models **after** services (models may `@OSGiService` inject services).

| Order | Model | Used By Component | Injection |
|-------|-------|------------------|-----------|
| 1 | `FAQItemModel` | faq (child) | `@ValueMapValue` question, answer |
| 2 | `FAQModel` | faq | `@ChildResource(name="faqItems")` |
| 3 | `TestimonialItemModel` | testimonial (child) | `@ValueMapValue` |
| 4 | `TestimonialModel` | testimonial | `@ChildResource` |
| 5 | `EmployeeCardModel` | employeecard | `@ValueMapValue`, `@PostConstruct` TagManager |
| 6 | `DepartmentCardModel` | departmentcard | `@ValueMapValue`, TagManager |
| 7 | `EmployeeSearchModel` | employeesearch | `@ValueMapValue`, `@OSGiService EmployeeService` |
| 8 | `AnnouncementBannerModel` | announcement | `@ValueMapValue`, `@OSGiService AnnouncementService` |

**Model pattern:**

```java
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class EmployeeCardModel {
    @ValueMapValue private String employeeName;
    @ValueMapValue(name = "cq:tags") private String[] tags;
    private List<String> tagTitles = new ArrayList<>();

    @PostConstruct
    void init() {
        // Resolve cq:tags to human-readable titles via TagManager
    }
}
```

**Register Sling Models package in bundle manifest** (`core/pom.xml`):

```xml
<Sling-Model-Packages>com.abc.employeehub.core.models</Sling-Model-Packages>
```

### 4.4 Servlets

**Package:** `servlets/`

Create servlets **last** — they `@Reference` services.

| Servlet | Path | Params | Service |
|---------|------|--------|---------|
| `EmployeeSearchServlet` | `/bin/employeehub/employee-search.json` | `q`, `limit` | EmployeeService |
| `DepartmentSearchServlet` | `/bin/employeehub/department-search.json` | `department` | DepartmentService |
| `AnnouncementServlet` | `/bin/employeehub/announcement.json` | — | AnnouncementService |

**Servlet pattern:**

```java
@Component(service = Servlet.class, property = {
    "sling.servlet.paths=/bin/employeehub/employee-search",
    "sling.servlet.methods=GET",
    "sling.servlet.extensions=json"
})
public class EmployeeSearchServlet extends SlingSafeMethodsServlet {
    @Reference private transient EmployeeService employeeService;
}
```

### 4.5 Verify core bundle

```bash
mvn clean install -PautoInstallSinglePackage
```

OSGi console → http://localhost:4502/system/console/bundles → search `employeehub.core` → status **Active**.

---

## Phase 5 — Page Component & Base Clientlibs

> Create the **page component first** — editable templates reference it. Follow the **Core WCM Page v3 pattern** (WKND-style): no custom `page.html`.

### 5.1 Page component

**Path:** `ui.apps/.../components/page/`

**`.content.xml`:**

```xml
<jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    jcr:primaryType="cq:Component"
    jcr:title="Employee Hub Page"
    componentGroup=".hidden"
    sling:resourceSuperType="core/wcm/components/page/v3/page"/>
```

**Do not create `page.html`.** Use these files instead:

**`body.html`** — `.eh-page` wrapper + TemplatedContainer:

```html
<div class="eh-page">
    <div class="eh-page-main">
        <sly data-sly-use.templatedContainer="com.day.cq.wcm.foundation.TemplatedContainer"
             data-sly-repeat.child="${templatedContainer.structureResources}"
             data-sly-resource="${child.path @ resourceType=child.resourceType, decorationTagName='div'}"/>
    </div>
</div>
```

**`customheaderlibs.html`** — CSS in all modes:

```html
<sly data-sly-use.clientlib="/libs/granite/sightly/templates/clientlib.html"
     data-sly-call="${clientlib.css @ categories='employeehub.base,employeehub.components'}"/>
```

**`customfooterlibs.html`** — JS in all modes:

```html
<sly data-sly-use.clientlib="/libs/granite/sightly/templates/clientlib.html"
     data-sly-call="${clientlib.js @ categories='employeehub.base'}"/>
```

> Scope CSS to `.eh-page` in `base.css` — avoid global `* { margin:0; padding:0 }` which breaks the authoring UI.

### 5.2 Base clientlib

**Path:** `ui.apps/.../clientlibs/clientlib-base/`

```
clientlib-base/
├── .content.xml          categories="[employeehub.base]"
├── css.txt               #base.css
├── css/base.css          # Design tokens, layout, typography
├── js.txt                #base.js
└── js/base.js            # Nav active-state helper
```

### 5.3 Components clientlib

**Path:** `clientlibs/clientlib-components/`

```
clientlib-components/
├── .content.xml          categories="[employeehub.components]"
├── css.txt
└── css/components.css    # All component styles
```

### 5.4 Deploy and verify

```bash
mvn clean install -PautoInstallSinglePackage
```

---

## Phase 6 — Structure Components

> These are **locked in the template structure** — authors cannot remove them.

Create in this order (dependencies flow top → bottom):

| Order | Component | Dialog | EditConfig | Used In |
|-------|-----------|--------|------------|---------|
| 1 | **navigation** | Yes | Yes | Inside header |
| 2 | **header** | Yes (logoText) | Yes | Both templates |
| 3 | **footer** | Yes (copyrightText) | Yes | Both templates |
| 4 | **breadcrumb** | No | No | Content template only |
| 5 | **hero** | Yes | Yes | Landing template only |

### 6.1 Header example

**`header.html`:**

```html
<header class="eh-header">
    <div class="eh-container eh-header__inner">
        <div class="eh-header__logo">
            <span class="eh-header__logo-icon">A</span>
            <span>${properties.logoText || 'ABC Employee Hub'}</span>
        </div>
        <sly data-sly-resource="${'navigation' @ resourceType='employeehub/components/navigation'}"/>
    </div>
</header>
```

**`navigation.html`:**

```html
<nav class="eh-header__nav">
    <a href="/content/employeehub/home.html">Home</a>
    <a href="/content/employeehub/employees.html">Employees</a>
    <a href="/content/employeehub/departments.html">Departments</a>
    <a href="/content/employeehub/faq.html">FAQ</a>
    <a href="/content/employeehub/contact.html">Contact</a>
</nav>
```

### 6.2 Component groups

| Group | Purpose |
|-------|---------|
| `.hidden` | Page component (not in side panel) |
| `Employee Hub - Common` | Structure + layout components |
| `Employee Hub - Business` | Domain-specific components |

---

## Phase 7 — Editable Templates & Policies (Before Business Components)

> **This is the most important phase for site architecture.**  
> Templates define page layout. Policies define which components authors can add.

### 7.1 Create Configuration folder

Editable templates live under:

```
/conf/employeehub/settings/wcm/templates/
```

**Two ways to create:**

**Option A — In AEM UI (recommended for learning):**

1. Go to **Tools → General → Configuration Browser**
2. Create configuration: `employeehub`
3. Go to **Tools → General → Templates → `/conf/employeehub`**
4. Create templates (see 7.3 and 7.4)

**Option B — Via code in ui.config (this project):**

```
ui.config/src/main/content/jcr_root/conf/employeehub/settings/wcm/templates/
├── employee-landing-template/
│   ├── .content.xml
│   ├── initial/.content.xml
│   ├── structure/.content.xml
│   └── policies/.content.xml
└── employee-content-template/
    ├── .content.xml
    ├── initial/.content.xml
    ├── structure/.content.xml
    └── policies/.content.xml
```

Deployed with `mvn clean install -PautoInstallSinglePackage`.

**Required conf folder hierarchy** (AEM Templates UI queries `cq:Page` at this path):

```
/conf/employeehub/                              [sling:Folder]
└── settings/                                 [sling:Folder]
    └── wcm/                                    [cq:Page]  ← required
        ├── template-types/page/                ← required for editable templates
        ├── templates/                          [cq:Page]  ← required (Templates console lists this)
        │   ├── employee-landing-template/
        │   └── employee-content-template/
        └── policies/                           [cq:Page]
```

> If `settings/wcm/templates` is `nt:folder` instead of `cq:Page`, templates exist in CRX but **do not appear** in `/libs/wcm/core/content/sites/templates.html/conf`. Delete `/conf/employeehub` and redeploy after fixing node types.

> Each template's **`policies/` node must be `cq:Page`**. If it is `nt:unstructured`, design JSON returns 404 and the **Content Tree stays empty** in the page editor.

### 7.2 Template anatomy — three layers

Every editable template has **three** sub-nodes:

| Layer | Purpose | Author Can Edit? |
|-------|---------|-----------------|
| **structure** | Fixed layout (header, footer, hero) | No — locked |
| **initial** | Default content when a new page is created | Sets starting content |
| **policies** | Allowed components per container | Controls side panel |

### 7.3 Template 1 — Employee Landing Template

**Use for:** Home page  
**Conf path:** `/conf/employeehub/settings/wcm/templates/employee-landing-template`

#### Template metadata (`.content.xml`)

```xml
<jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    jcr:primaryType="cq:Template"
    jcr:title="Employee Landing Template"
    status="enabled">
    <jcr:content
        jcr:primaryType="cq:PageContent"
        jcr:title="Employee Landing Template"
        sling:resourceType="employeehub/components/page"/>
</jcr:root>
```

#### Structure layer (`structure/.content.xml`)

```
┌─────────────────────────────────────┐
│  header          (LOCKED)           │
├─────────────────────────────────────┤
│  hero            (LOCKED)           │
├─────────────────────────────────────┤
│  main            (EDITABLE)         │  ← authors drag components here
├─────────────────────────────────────┤
│  footer          (LOCKED)           │
└─────────────────────────────────────┘
```

```xml
<jcr:root jcr:primaryType="cq:Page" xmlns:...>
    <jcr:content sling:resourceType="employeehub/components/page">
        <root sling:resourceType="wcm/foundation/components/responsivegrid">
            <header sling:resourceType="employeehub/components/header"/>
            <hero   sling:resourceType="employeehub/components/hero"/>
            <main   sling:resourceType="wcm/foundation/components/responsivegrid"
                    cq:policy="employeehub/landing-main"
                    editable="{Boolean}true"/>
            <footer sling:resourceType="employeehub/components/footer"/>
        </root>
    </jcr:content>
</jcr:root>
```

> **Important:** Use `employeehub/components/page` in the **structure** layer (with Core Page v3 supertype). Set `cq:templateType` on the template definition. Template **`policies/.content.xml`** must be **`cq:Page`**.

#### Policy layer (`policies/.content.xml`)

Must be **`jcr:primaryType="cq:Page"`** with embedded policy on `main`:

```xml
<main
    sling:resourceType="wcm/core/components/policy/policy"
    components="[
        /apps/employeehub/components/announcement,
        /apps/employeehub/components/employeespotlight,
        /apps/employeehub/components/quicklinks,
        /apps/employeehub/components/testimonial,
        /apps/employeehub/components/title,
        /apps/employeehub/components/text,
        /apps/employeehub/components/image
    ]"/>
```

**In AEM UI:** Template Editor → select **Main** area → Policy → add allowed components from the list above.

### 7.4 Template 2 — Employee Content Template

**Use for:** Employees, Departments, FAQ, Contact  
**Conf path:** `/conf/employeehub/settings/wcm/templates/employee-content-template`

#### Structure layer

```
┌─────────────────────────────────────┐
│  header          (LOCKED)           │
├─────────────────────────────────────┤
│  breadcrumb      (LOCKED)           │
├─────────────────────────────────────┤
│  main            (EDITABLE)         │
├─────────────────────────────────────┤
│  footer          (LOCKED)           │
└─────────────────────────────────────┘
```

```xml
<header     sling:resourceType="employeehub/components/header"/>
<breadcrumb sling:resourceType="employeehub/components/breadcrumb"/>
<main       sling:resourceType="wcm/foundation/components/responsivegrid"
            editable="{Boolean}true"/>
<footer     sling:resourceType="employeehub/components/footer"/>
```

#### Policy layer — allowed components on Main

```
employeesearch, employeecard, departmentcard, faq,
title, text, image, gridcontainer, contactcards
```

```xml
<main
    sling:resourceType="wcm/core/components/policy/policy"
    components="[
        /apps/employeehub/components/employeesearch,
        /apps/employeehub/components/employeecard,
        /apps/employeehub/components/departmentcard,
        /apps/employeehub/components/faq,
        /apps/employeehub/components/title,
        /apps/employeehub/components/text,
        /apps/employeehub/components/image,
        /apps/employeehub/components/gridcontainer,
        /apps/employeehub/components/contactcards
    ]"/>
```

### 7.5 Enable templates & create site

**In AEM UI:**

1. **Tools → Templates → `/conf/employeehub`** → enable both templates
2. **Sites → Create Site** → choose conf `employeehub` → site name `employeehub`
3. Site root: `/content/employeehub`

### 7.6 Deploy templates

Templates deploy with code — no separate ui.content install needed:

```bash
mvn clean install -PautoInstallSinglePackage
```

Verify: http://localhost:4502/libs/wcm/core/content/sites/templates.html/conf/employeehub

If created in AEM UI instead — no deploy needed; templates are already in JCR.

---

## Phase 8 — Common Components

> Build these **after** templates/policies are defined. All are in policy allowed lists.

| Order | Component | Dialog | EditConfig | Notes |
|-------|-----------|--------|------------|-------|
| 1 | **title** | Core | Yes | Supertype `core/wcm/components/title/v3/title` |
| 2 | **text** | Core | Yes | Supertype `core/wcm/components/text/v2/text` |
| 3 | **image** | Core | No | Supertype `core/wcm/components/image/v3/image` |
| 4 | **gridcontainer** | No | Yes | Container (`cq:isContainer=true`) |
| 5 | **contactcards** | No | No | Static HR / IT / Office cards |

**Component file structure (every component):**

```
components/title/
├── .content.xml       # jcr:title, componentGroup
└── title.html         # HTL template
```

**Title example (`title.html`):**

```html
<div class="eh-container">
    <h2 class="eh-title">${properties.jcr:title || properties.text}</h2>
</div>
```

---

## Phase 9 — Business Components (With Models & Dialogs)

> Each business component needs: HTL + `.content.xml` + `_cq_dialog` + Sling Model in core.

Build in this order (simplest → most complex):

| Order | Component | Model | Dialog | Multifield? |
|-------|-----------|-------|--------|------------|
| 1 | **announcement** | AnnouncementBannerModel | Yes | No |
| 2 | **employeecard** | EmployeeCardModel | Yes | No |
| 3 | **departmentcard** | DepartmentCardModel | Yes | No |
| 4 | **employeesearch** | EmployeeSearchModel | Yes | No |
| 5 | **employeespotlight** | — (properties only) | Optional | No |
| 6 | **quicklinks** | — (static links) | No | No |
| 7 | **faq** | FAQModel + FAQItemModel | Yes | **Yes** |
| 8 | **testimonial** | TestimonialModel + TestimonialItemModel | Yes | **Yes** |

### 9.1 Component with dialog — Employee Card

**Dialog fields** (`_cq_dialog/.content.xml`):

| Field | Type | Property |
|-------|------|----------|
| Employee Name | textfield | `./employeeName` |
| Email | textfield | `./email` |
| Department | textfield | `./department` |
| Photo | pathfield | `./photo` |
| Employee ID | textfield | `./employeeId` |

Tags are set via the **Tags** tab in component edit (standard `cq:tags`).

**HTL (`employeecard.html`):**

```html
<div class="eh-card eh-employee-card"
     data-sly-use.model="com.abc.employeehub.core.models.EmployeeCardModel">
    <div data-sly-test="${!model.photo}" class="${model.avatarClass}">${model.initials}</div>
    <h3>${model.employeeName}</h3>
    <p>${model.department}</p>
    <p>${model.email}</p>
    <span class="eh-tag" data-sly-repeat.tag="${model.tagTitles}">${tag}</span>
</div>
```

### 9.2 Multifield dialog — FAQ

**Dialog structure:**

```xml
<faqItems
    sling:resourceType="granite/ui/components/coral/foundation/form/multifield"
    composite="{Boolean}true"
    fieldLabel="FAQ Items">
    <field name="./faqItems">
        <items>
            <question sling:resourceType="granite/.../textfield" name="./question"/>
            <answer   sling:resourceType="granite/.../textarea"  name="./answer"/>
        </items>
    </field>
</faqItems>
```

Stored in JCR as:

```
faq/
└── faqItems/
    ├── item0/   (question, answer)
    ├── item1/
    └── item2/
```

**Model reads multifield:**

```java
@ChildResource(name = "faqItems")
private List<FAQItemModel> faqItems;
```

### 9.3 Employee Search component

Connects UI to servlet:

```html
<div class="eh-search" data-endpoint="${model.searchEndpoint}">
    <input class="eh-search__input" placeholder="${model.placeholder}"/>
    <button class="eh-btn eh-search__btn">${model.buttonText}</button>
</div>
<div class="eh-search__results"></div>
```

`EmployeeSearchModel.getSearchEndpoint()` returns `/bin/employeehub/employee-search.json`.

### 9.4 Deploy business components

```bash
mvn clean install -PautoInstallSinglePackage
```

Verify components appear in side panel when editing a page (only those allowed by template policy).

---

## Phase 10 — Feature Clientlibs (Search & FAQ JS)

| Clientlib | Category | Used By |
|-----------|----------|---------|
| `clientlib-search` | `employeehub.search` | employeesearch component |
| `clientlib-faq` | `employeehub.faq` | faq component |

**Include in component HTL:**

```html
<sly data-sly-use.clientlib="/libs/granite/sightly/templates/clientlib.html"
     data-sly-call="${clientlib.js @ categories='employeehub.search'}"/>
```

**search.js** — fetches `/bin/employeehub/employee-search.json?q=...` and renders results.

**faq.js** — accordion toggle on `.eh-faq__question` click.

---

## Phase 11 — Content, Tags & Data in AEM

> **Content is authored in AEM**, not deployed via Maven auto-install.  
> Follow this order:

```
1. Tags  →  2. Data nodes  →  3. Site & pages  →  4. Page content
```

### 11.1 Create tags

**Tools → Tags → Create Namespace**

Namespace: `employeehub`

```
employeehub/
├── department/
│   ├── it
│   ├── hr
│   ├── finance
│   ├── operations
│   └── marketing
├── role/
│   ├── manager
│   ├── developer
│   ├── architect
│   ├── analyst
│   └── intern
└── employment-type/
    ├── permanent
    ├── contract
    └── part-time
```

JCR path: `/content/cq:tags/employeehub/`

### 11.2 Create employee data nodes

**CRX DE Lite** or **Sites → Create Page** under a data folder.

Path: `/content/employeehub/data/employees/`

Create one node per employee:

| Node | employeeName | email | department | employeeId | cq:tags |
|------|-------------|-------|------------|------------|---------|
| `john-doe` | John Doe | john.doe@abc.com | Information Technology | EMP001 | department/it, role/manager, employment-type/permanent |
| `sarah-smith` | Sarah Smith | sarah.smith@abc.com | Human Resources | EMP002 | department/hr, role/manager, employment-type/permanent |
| `mike-johnson` | Mike Johnson | mike.johnson@abc.com | Finance | EMP003 | department/finance, role/analyst, employment-type/permanent |

> These nodes are what `EmployeeService` queries when the search servlet is called.

### 11.3 Create department data nodes

Path: `/content/employeehub/data/departments/`

| Node | departmentName | manager | employeeCount |
|------|-------------|---------|--------------|
| `it` | Information Technology | John Doe | 45 |
| `hr` | Human Resources | Sarah Smith | 12 |
| `finance` | Finance | Mike Johnson | 28 |

### 11.4 Create site and pages

**Sites → Create Site** (conf: `employeehub`) → site name: `employeehub`

| Page | Template | Path |
|------|----------|------|
| Home | Employee Landing Template | `/content/employeehub/home` |
| Employees | Employee Content Template | `/content/employeehub/employees` |
| Departments | Employee Content Template | `/content/employeehub/departments` |
| FAQ | Employee Content Template | `/content/employeehub/faq` |
| Contact | Employee Content Template | `/content/employeehub/contact` |

### 11.5 Compose page content (Edit mode)

**Home page — drag into Main area:**

1. Announcement Banner
2. Quick Links
3. Employee Spotlight (set employeeName, designation, spotlightText)
4. Testimonial (configure multifield items)

**Employees page — drag into Main area:**

1. Title → "Employee Directory"
2. Text → intro paragraph
3. Employee Search
4. Grid Container → inside it add 3 × Employee Card (or add cards as children)

**FAQ page:**

1. Title → "Frequently Asked Questions"
2. FAQ → configure multifield Q&A items

**Contact page:**

1. Title → "Contact Us"
2. Text → intro
3. Contact Cards

### 11.6 Optional — seed content from repo

One-time install of reference content:

```
Package Manager → Upload → ui.content/target/employeehub.ui.content-1.0.0-SNAPSHOT.zip → Install
```

---

## Phase 12 — All Module & Maven Deploy Profile

### 12.1 All module (code-only container)

**File:** `all/pom.xml`

Packages for deploy:
- `employeehub.ui.apps` (components + embedded core bundle)
- `employeehub.ui.config` (OSGi configs + `/conf/employeehub` templates)

Does **NOT** include `ui.content` (pages, tags, sample data — authored in AEM).

```xml
<subPackages>
    <subPackage>
        <groupId>com.abc.employeehub</groupId>
        <artifactId>employeehub.ui.apps</artifactId>
        <filter>true</filter>
    </subPackage>
    <subPackage>
        <groupId>com.abc.employeehub</groupId>
        <artifactId>employeehub.ui.config</artifactId>
        <filter>true</filter>
    </subPackage>
</subPackages>
```

### 12.2 autoInstallSinglePackage profile (parent pom.xml)

Activated with `-PautoInstallSinglePackage`:

- Builds all modules
- Installs `employeehub.all` container to `http://localhost:4502`
- Extracts and installs sub-packages (`extractSubPackages=true`)

---

## Deployment Commands

### Code deploy (day-to-day)

```bash
# AEM Author must be running on localhost:4502
mvn clean install -PautoInstallSinglePackage
```

Deploys: components, HTL, clientlibs, core OSGi bundle, OSGi configs, **editable templates**.

Does **NOT** deploy: pages, tags, employee data (author in AEM).

### Build only (no AEM)

```bash
mvn clean install
```

### Custom host / credentials

```bash
mvn clean install -PautoInstallSinglePackage \
  -Daem.host=localhost \
  -Daem.port=4502 \
  -Daem.user=admin \
  -Daem.password=admin
```

### Deploy to Publish (same package, different port)

Install the same container package on Publish so **`config.publish/`** OSGi values apply:

```bash
mvn clean install -PautoInstallSinglePackage \
  -Daem.host=localhost \
  -Daem.port=4503 \
  -Daem.user=admin \
  -Daem.password=admin
```

Verify Publish ConfigMgr → `environmentName=publish`, announcement banner color `#004499`.

### What to deploy after each change

| You changed… | Deploy command |
|-------------|---------------|
| Java (service, servlet, model) | `mvn clean install -PautoInstallSinglePackage` |
| Component HTL / dialog | `mvn clean install -PautoInstallSinglePackage` |
| Clientlib CSS/JS | `mvn clean install -PautoInstallSinglePackage` |
| OSGi config + templates (ui.config) | `mvn clean install -PautoInstallSinglePackage` |
| Template policy (in AEM UI) | No deploy — saved in JCR |
| Page content / tags / data | No deploy — authored in AEM |

---

## Demo Flow & Test URLs

### Pages

| Page | URL |
|------|-----|
| Home (editor) | http://localhost:4502/editor.html/content/employeehub/home.html |
| Home (publish view) | http://localhost:4502/content/employeehub/home.html?wcmmode=disabled |
| Employees | http://localhost:4502/content/employeehub/employees.html |
| Departments | http://localhost:4502/content/employeehub/departments.html |
| FAQ | http://localhost:4502/content/employeehub/faq.html |
| Contact | http://localhost:4502/content/employeehub/contact.html |
| Templates | http://localhost:4502/libs/wcm/core/content/sites/templates.html/conf/employeehub |

### Authoring verification

- [ ] Content Tree shows structure components under page root
- [ ] Open Dialog works on components with `_cq_dialog`
- [ ] Page styled in Edit mode (not only View as Published)
- [ ] Side panel lists policy-allowed components

Clean view: append `?wcmmode=disabled`

### Servlet tests

```bash
# Employee search
curl -u admin:admin "http://localhost:4502/bin/employeehub/employee-search.json?q=john"

# All departments
curl -u admin:admin "http://localhost:4502/bin/employeehub/department-search.json"

# Single department
curl -u admin:admin "http://localhost:4502/bin/employeehub/department-search.json?department=Human%20Resources"

# Announcement (OSGi config)
curl -u admin:admin "http://localhost:4502/bin/employeehub/announcement.json"
```

### OSGi verification

| Check | URL |
|-------|-----|
| Core bundle active | http://localhost:4502/system/console/bundles → `employeehub.core` |
| OSGi configs | http://localhost:4502/system/console/configMgr |
| Employee data | http://localhost:4502/crx/de/index.jsp → `/content/employeehub/data/employees` |

### Demo order

```
1. Home         → hero, announcement, quick links, spotlight, testimonials
2. Employees    → live search: "john", "hr", "finance"
3. Servlets     → employee-search.json, department-search.json, announcement.json
4. Departments  → department cards
5. FAQ          → accordion + multifield authoring demo
6. Contact      → contact cards
7. OSGi console → bundle + configs
```

---

## Reference — Components, Services, Servlets

### All components (19)

| Component | Group | Model | Dialog | EditConfig |
|-----------|-------|-------|--------|------------|
| page | hidden | — | — | — |
| header | Common | — | Yes | Yes |
| footer | Common | — | Yes | Yes |
| navigation | Common | — | Yes | Yes |
| breadcrumb | Common | — | — | — |
| hero | Common | — | Yes | Yes |
| title | Common | — | Core | Yes |
| text | Common | — | Core | Yes |
| image | Common | — | Core | — |
| gridcontainer | Common | — | — | Yes |
| contactcards | Common | — | — | — |
| announcement | Business | AnnouncementBannerModel | Yes | Yes |
| employeecard | Business | EmployeeCardModel | Yes | Yes |
| employeesearch | Business | EmployeeSearchModel | Yes | Yes |
| departmentcard | Business | DepartmentCardModel | Yes | Yes |
| faq | Business | FAQModel | Yes (multifield) | Yes |
| testimonial | Business | TestimonialModel | Yes (multifield) | Yes |
| quicklinks | Business | — | Yes | Yes |
| employeespotlight | Business | — | Yes | Yes |

### Templates & policies

| Template | Pages | Structure | Main Policy Allows |
|----------|-------|-----------|-------------------|
| Employee Landing | Home | header, hero, main, footer | announcement, spotlight, quicklinks, testimonial, title, text, image |
| Employee Content | Inner pages | header, breadcrumb, main, footer | employeesearch, employeecard, departmentcard, faq, title, text, image, gridcontainer, contactcards |

### Services & servlets

| Layer | Name | Path / Detail |
|-------|------|--------------|
| Service | EmployeeService | `/content/employeehub/data/employees` |
| Service | DepartmentService | `/content/employeehub/data/departments` |
| Service | AnnouncementService | OSGi AnnouncementConfiguration |
| Service | FAQService | JCR utility |
| Servlet | EmployeeSearchServlet | `/bin/employeehub/employee-search.json?q=&limit=` |
| Servlet | DepartmentSearchServlet | `/bin/employeehub/department-search.json?department=` |
| Servlet | AnnouncementServlet | `/bin/employeehub/announcement.json` |

### Clientlibs

| Clientlib | Category | Contents |
|-----------|----------|----------|
| clientlib-base | employeehub.base | Design tokens, layout CSS, base JS |
| clientlib-components | employeehub.components | Component CSS |
| clientlib-search | employeehub.search | Employee search AJAX |
| clientlib-faq | employeehub.faq | FAQ accordion JS |

---

## Troubleshooting

| Issue | Cause | Fix |
|-------|-------|-----|
| `aem-sdk-api` not found | SDK JAR not in local Maven | Phase 0.2 — install locally |
| Content Tree empty | Template `policies/` not `cq:Page` / design JSON 404 | Set policies as `cq:Page`; verify design JSON returns 200 |
| Open Dialog does nothing | Bad `_cq_editConfig` or missing `_cq_dialog` | Use `cq:actions="[edit,delete,copymove,insert]"`; add dialog |
| Styled in publish only | Clientlibs blocked in edit mode | Load CSS in `customheaderlibs` (all modes); scope to `.eh-page` |
| Servlet 404 | Bundle not active or path not registered | Start bundle; check SlingServletResolver config |
| Search returns empty | No employee data nodes | Create nodes at `/content/employeehub/data/employees` or install ui.content |
| Components not in side panel | Not in template policy | Add to policy `main` allowed components list |
| Templates not in AEM UI | ui.config not deployed or wrong node type | Deploy ui.config; `wcm/templates` must be `cq:Page`; add `template-types` |
| Missing `cq:conf` on site | Site root not linked to conf | Set `cq:conf="/conf/employeehub"` in ui.content |
| OSGi configs missing after deploy | ui.apps filter owns full `/apps/employeehub` | Narrow ui.apps filter; osgiconfig stays in ui.config |
| Wrong runmode OSGi values | Config only in `config/` not runmode folders | Use `config.author/` and `config.publish/`; verify in ConfigMgr per instance |
| Core bundle Installed not Active | OSGi error on startup | Check OSGi console for errors; fix imports |
| Package ACL validation error | Strict vault validation | `accessControlHandling=ignore`, `skipValidation=true` in pom |
| `mvn install` tries to deploy without profile | Old all/pom config | Install only runs with `-PautoInstallSinglePackage` |
| UI changes not visible | Code not deployed | `mvn clean install -PautoInstallSinglePackage` |
| OSGi configs missing after deploy | ui.apps filter owns full `/apps/employeehub` | Narrow ui.apps filter; osgiconfig stays in ui.config |

---

## Architecture Diagram

```
                         ┌─────────────────────────────────┐
                         │         AEM Author (:4502)       │
                         └─────────────────────────────────┘
                                          │
          ┌───────────────────────────────┼───────────────────────────────┐
          │                               │                               │
   /content/employeehub            /apps/employeehub              /conf/employeehub
   (authored in AEM)              (Maven deploy)                  (templates/policies)
          │                               │                               │
   ┌──────┴──────┐              ┌─────────┴─────────┐            ┌───────┴───────┐
   │ pages       │              │ components (HTL)  │            │ landing tmpl  │
   │ data/       │              │ clientlibs        │            │ content tmpl  │
   │ tags        │              │ core bundle (OSGi)│            │ policies      │
   └──────┬──────┘              └─────────┬─────────┘            └───────────────┘
          │                               │
          │         ┌─────────────────────┤
          │         │                     │
          │    Sling Models          Servlets (/bin/employeehub/*.json)
          │         │                     │
          └─────────┴────── Services ─────┘
                              │
                    EmployeeService / DepartmentService
                              │
                    /content/employeehub/data/*
```

---

## License

Internal use — ABC Corporation.
