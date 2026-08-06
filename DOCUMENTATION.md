# ABC Employee Hub — Project Documentation

Complete reference for templates, policies, OSGi, services, servlets, components, pages, and content.

| Item | Value |
|------|-------|
| **Project path** | `/Users/<user>/Desktop/employeehub` |
| **Context path** | `/conf/employeehub` |
| **Site path** | `/content/employeehub` |
| **appId** | `employeehub` |
| **groupId** | `com.abc.employeehub` |
| **Page component** | `employeehub/components/page` (extends Core WCM Page v3) |

---

## Table of contents

1. [Architecture overview](#1-architecture-overview)
2. [Maven modules](#2-maven-modules)
3. [Configuration context](#3-configuration-context-confemployeehub)
4. [Template type](#4-template-type)
5. [Editable templates](#5-editable-templates)
6. [Policies](#6-policies)
7. [OSGi configuration](#7-osgi-configuration)
8. [Services](#8-services-osgi-components)
9. [Servlets](#9-servlets)
10. [Sling models](#10-sling-models)
11. [Components](#11-components)
12. [Pages and site structure](#12-pages-and-site-structure)
13. [Data nodes](#13-data-nodes)
14. [Tags and DAM](#14-tags-and-dam)
15. [Clientlibs](#15-clientlibs)
16. [End-to-end flows](#16-end-to-end-flows)
17. [File index](#17-file-index-quick-reference)
18. [Troubleshooting](#18-troubleshooting)

---

## 1. Architecture overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         AEM Author                               │
├──────────────┬──────────────┬──────────────┬────────────────────┤
│  ui.content  │   ui.apps    │  ui.config   │       core         │
│  Pages       │  Components  │  Templates   │  Services          │
│  Data nodes  │  HTL         │  Policies    │  Servlets          │
│  Tags        │  Clientlibs  │  OSGi cfg    │  Sling Models      │
└──────────────┴──────────────┴──────────────┴────────────────────┘
         │              │              │              │
         └──────────────┴──────────────┴──────────────┘
                              │
                    /content/employeehub
                    /conf/employeehub
                    /apps/employeehub
```

| Layer | Module | Deploys to | Purpose |
|-------|--------|------------|---------|
| Code | `core` | Embedded in `/apps/employeehub/install` | Java: services, servlets, Sling models |
| Code | `ui.apps` | `/apps/employeehub` | Components, HTL, clientlibs |
| Config | `ui.config` | `/conf/employeehub`, `/apps/employeehub/osgiconfig` | Templates, policies, OSGi `.cfg.json` |
| Content | `ui.content` | `/content/employeehub`, DAM, tags | Seed pages and data (installed separately) |
| Package | `all` | Container | Bundles `ui.apps` + `ui.config` only |

**Deploy command:**

```bash
cd /Users/<user>/Desktop/employeehub
mvn clean install -PautoInstallSinglePackage -Daem.port=4502
```

**ui.content** (not in `all` package):

```bash
cd ui.content
mvn clean install
mvn com.day.jcr.vault:content-package-maven-plugin:1.0.4:install \
  -Dvault.file=target/employeehub.ui.content-1.0.0-SNAPSHOT.zip \
  -Daem.host=localhost -Daem.port=4502 \
  -Daem.user=admin -Daem.password=admin
```

---

## 2. Maven modules

```
employeehub/                    (parent POM, packaging=pom)
├── core/                       OSGi bundle
├── ui.apps/                    content-package (application)
├── ui.config/                  content-package (application)
├── ui.content/                 content-package (content)
└── all/                        container package
```

| Module | Artifact | Packaging | Notes |
|--------|----------|-----------|-------|
| `core` | `employeehub.core` | bundle | Export-Package: `com.abc.employeehub.core.*`; Sling-Model-Packages: `com.abc.employeehub.core.models` |
| `ui.apps` | `employeehub.ui.apps` | content-package | Embeds core bundle at `/apps/employeehub/install` |
| `ui.config` | `employeehub.ui.config` | content-package | `/conf/employeehub` + osgiconfig |
| `ui.content` | `employeehub.ui.content` | content-package | Excluded from `autoInstallSinglePackage` |
| `all` | `employeehub.all` | container | Sub-packages: ui.apps + ui.config |

**Vault filters:**

| Module | Filter roots |
|--------|--------------|
| ui.apps | `/apps/employeehub/components`, `/apps/employeehub/clientlibs`, `/apps/employeehub/install` |
| ui.config | `/apps/employeehub/osgiconfig`, `/conf/employeehub` |
| ui.content | `/content/employeehub`, `/content/dam/employeehub`, `/content/cq:tags/employeehub` |

---

## 3. Configuration context (`/conf/employeehub`)

**Repo path:** `ui.config/src/main/content/jcr_root/conf/employeehub/`

```
/conf/employeehub                          (sling:Folder)
└── settings/
    └── wcm/
        ├── template-types/page/           Base template type
        ├── templates/
        │   ├── employee-landing-template/
        │   └── employee-content-template/
        └── policies/                      Reusable policy definitions
```

**Site link** — required on site root (`ui.content/.../content/employeehub/.content.xml`):

```xml
cq:conf="/conf/employeehub"
```

Without `cq:conf`, editable templates and policies do not merge correctly in the page editor.

**Allowed templates** on site root:

```
/conf/employeehub/settings/wcm/templates/employee-landing-template
/conf/employeehub/settings/wcm/templates/employee-content-template
```

---

## 4. Template type

**Path:** `/conf/employeehub/settings/wcm/template-types/page/`

| Layer | File | Purpose |
|-------|------|---------|
| Definition | `page/.content.xml` | `cq:Template`, title "Page" |
| Structure | `page/structure/.content.xml` | Base layout: `employeehub/components/page` + editable `root/container` responsivegrid |
| Initial | `page/initial/.content.xml` | Minimal scaffold for new pages |
| Policies | `page/policies/.content.xml` | Policy mapping scaffold at type level |

Each editable template references this type:

```xml
cq:templateType="/conf/employeehub/settings/wcm/template-types/page"
```

---

## 5. Editable templates

Both templates use **`employeehub/components/page`** in the **structure** layer.

### 5.1 Employee Landing Template

**Path:** `/conf/employeehub/settings/wcm/templates/employee-landing-template/`

| Layer | Repo file | Purpose |
|-------|-----------|---------|
| Definition | `.content.xml` | Metadata, enabled, template-type link |
| Structure | `structure/.content.xml` | Fixed page shell (header, hero, main, footer) |
| Initial | `initial/.content.xml` | Default content for new pages |
| Policies | `policies/.content.xml` | **Must be `cq:Page`** — design JSON for editor |

**Structure layout:**

```
root (wcm/foundation/components/responsivegrid)
├── header      → employeehub/components/header
├── hero        → employeehub/components/hero
├── main        → responsivegrid, editable=true, cq:policy="employeehub/landing-main"
└── footer      → employeehub/components/footer
```

**Main area allowed components** (from `policies/.content.xml`):

- `/apps/employeehub/components/announcement`
- `/apps/employeehub/components/employeespotlight`
- `/apps/employeehub/components/quicklinks`
- `/apps/employeehub/components/image`
- `/apps/employeehub/components/title`
- `/apps/employeehub/components/text`
- `/apps/employeehub/components/testimonial`
- `group:Employee Hub`

**Used by:** Home page, site root default template

---

### 5.2 Employee Content Template

**Path:** `/conf/employeehub/settings/wcm/templates/employee-content-template/`

**Structure layout:**

```
root (responsivegrid)
├── header      → employeehub/components/header
├── breadcrumb  → employeehub/components/breadcrumb
├── main        → responsivegrid, editable=true, cq:policy="employeehub/content-main"
└── footer      → employeehub/components/footer
```

**Main area allowed components:**

- employeesearch, employeecard, departmentcard, faq, text, image, title, gridcontainer, contactcards
- `group:Employee Hub`

**Nested gridcontainer policy** (in content template `policies/.content.xml`):

- Allows departmentcard and employeecard inside grid containers

**Used by:** Employees, Departments, FAQ, Contact pages

---

### Template policies and Content Tree

The page editor loads design via:

```
/conf/employeehub/settings/wcm/templates/<template>/policies/_jcr_content.{timestamp}.json
```

If `policies/` is `nt:unstructured` instead of **`cq:Page`**, this returns **404**, the editor never finishes init, and the **Content Tree stays empty**.

Verify:

```bash
curl -u admin:admin -o /dev/null -w "%{http_code}\n" \
  "http://localhost:4502/conf/employeehub/settings/wcm/templates/employee-landing-template/policies/_jcr_content.1500560231670.json"
```

Expect **200** (timestamp may differ).

---

## 6. Policies

Policies control which components appear in the side panel for each editable area.

### Global policy definitions

**Path:** `ui.config/.../conf/employeehub/settings/wcm/policies/`

| Policy ID | Path suffix | Applies to | Allowed components |
|-----------|-------------|------------|-------------------|
| `employeehub/landing-main` | `.../responsivegrid/employeehub/landing-main/` | Landing `main` | announcement, employeespotlight, quicklinks, image, title, text, testimonial |
| `employeehub/content-main` | `.../responsivegrid/employeehub/content-main/` | Content `main` | employeesearch, employeecard, departmentcard, faq, text, image, title, gridcontainer, contactcards |
| `gridcontainer-main` | `.../gridcontainer/gridcontainer-main/` | Grid container | departmentcard, employeecard |

Structure nodes reference policies via:

```xml
cq:policy="employeehub/landing-main"
```

Template-level `policies/.content.xml` embeds the same allow-lists using `sling:resourceType="wcm/core/components/policy/policy"` on policy nodes.

---

## 7. OSGi configuration

**Path:** `ui.config/src/main/content/jcr_root/apps/employeehub/osgiconfig/`

```
osgiconfig/
├── config/              ← all run modes (infrastructure)
├── config.author/       ← author-only application settings
└── config.publish/      ← publish-only application settings
```

| Folder | Config file | Purpose |
|--------|-------------|---------|
| `config/` | `RepositoryInitializer~employeehub.cfg.json` | Creates service user; grants read on data and DAM paths |
| `config/` | `ServiceUserMapperImpl.amended~employeehub.cfg.json` | Maps bundle subservice for FAQ service |
| `config/` | `SlingServletResolver~employeehub.cfg.json` | Registers servlet paths `/bin/` and `/bin/employeehub/` |
| `config.author/` | `EmployeeHubConfiguration.cfg.json` | Author portal settings (higher search limit) |
| `config.author/` | `AnnouncementConfiguration.cfg.json` | Author preview announcement banner |
| `config.publish/` | `EmployeeHubConfiguration.cfg.json` | Publish portal settings |
| `config.publish/` | `AnnouncementConfiguration.cfg.json` | Production announcement banner |

AEM loads `config/` on every instance, then merges **`config.author`** on Author or **`config.publish`** on Publish based on run mode.

### Current values — Author (`config.author/`)

**EmployeeHubConfiguration:**

```
portalName="ABC Employee Hub"
environmentName="author"
defaultDepartment="all"
maximumSearchResults=25
```

**AnnouncementConfiguration:**

```
announcementMessage="[Author Preview] Welcome to ABC Employee Hub — content authoring environment."
bannerColor="#0066cc"
enableBanner=true
```

### Current values — Publish (`config.publish/`)

**EmployeeHubConfiguration:**

```
portalName="ABC Employee Hub"
environmentName="publish"
defaultDepartment="all"
maximumSearchResults=15
```

**AnnouncementConfiguration:**

```
announcementMessage="Welcome to ABC Employee Hub! Stay connected with company updates."
bannerColor="#004499"
enableBanner=true
```

### Shared — all run modes (`config/`)

**RepositoryInitializer~employeehub.cfg.json:**

```
create service user employeehub-service
set ACL for employeehub-service
  allow jcr:read on /content/employeehub/data
  allow jcr:read on /content/dam/employeehub
end
```

### Java configuration interfaces

**Path:** `core/src/main/java/com/abc/employeehub/core/configurations/`

| Interface | Attributes |
|-----------|------------|
| `EmployeeHubConfiguration` | `portalName`, `environmentName`, `defaultDepartment`, `maximumSearchResults` (default 20) |
| `AnnouncementConfiguration` | `announcementMessage`, `bannerColor`, `enableBanner` |

Both use `@ObjectClassDefinition`. Consumed via `@Designate` on `EmployeeServiceImpl` and `AnnouncementServiceImpl`.

| Instance | ConfigMgr URL | Expected `environmentName` |
|----------|---------------|---------------------------|
| Author | http://localhost:4502/system/console/configMgr | `author` |
| Publish | http://localhost:4503/system/console/configMgr | `publish` |

---

## 8. Services (OSGi components)

**Path:** `core/src/main/java/com/abc/employeehub/core/services/`

| Service | Implementation | Data source | Key methods |
|---------|----------------|-------------|-------------|
| `EmployeeService` | `EmployeeServiceImpl` | `/content/employeehub/data/employees` | `getEmployee()`, `searchEmployees()` — matches name, email, department |
| `DepartmentService` | `DepartmentServiceImpl` | `/content/employeehub/data/departments` | `getDepartment()`, `getAllDepartments()`, `getManagerDetails()`, `getDepartmentCount()` |
| `AnnouncementService` | `AnnouncementServiceImpl` | OSGi `AnnouncementConfiguration` | Configured title, message, color, enabled state |
| `FAQService` | `FAQServiceImpl` | JCR via service user `employeehub-service` | `loadFAQs(path)`, `sortFAQs()` — reads `faqItems` child nodes |

All implementations are `@Component(service = XxxService.class)`.

---

## 9. Servlets

**Path:** `core/src/main/java/com/abc/employeehub/core/servlets/`

All extend `SlingSafeMethodsServlet`, registered with `@Component(service = Servlet.class)`.

| Servlet | URL | Method | Parameters | Response |
|---------|-----|--------|------------|----------|
| `EmployeeSearchServlet` | `/bin/employeehub/employee-search.json` | GET | `q` (required), `limit` (optional) | `{ success, count, employees[] }` |
| `DepartmentSearchServlet` | `/bin/employeehub/department-search.json` | GET | `department` (optional) | All departments, or manager details for one dept |
| `AnnouncementServlet` | `/bin/employeehub/announcement.json` | GET | — | `{ title, message, bannerColor, enabled }` from OSGi config |

**Smoke tests:**

```bash
curl -u admin:admin "http://localhost:4502/bin/employeehub/employee-search.json?q=john"
curl -u admin:admin "http://localhost:4502/bin/employeehub/department-search.json"
curl -u admin:admin "http://localhost:4502/bin/employeehub/announcement.json"
```

Requires: core bundle **Active**, `SlingServletResolver~employeehub` config deployed.

---

## 10. Sling models

**Path:** `core/src/main/java/com/abc/employeehub/core/models/`

Registered via `Sling-Model-Packages: com.abc.employeehub.core.models` in `core/pom.xml`.

| Model | Adaptables | Component | Key API |
|-------|------------|-----------|---------|
| `AnnouncementBannerModel` | `Resource` | announcement | Component props override OSGi; `getTitle()`, `getMessage()`, `getBannerColor()`, `isEnabled()` |
| `EmployeeCardModel` | `Resource` | employeecard | `getEmployeeName()`, `getEmail()`, `getDepartment()`, `getPhoto()`, `getEmployeeId()`, `getInitials()`, `getTags()` |
| `DepartmentCardModel` | `Resource` | departmentcard | `getDepartmentName()`, `getManager()`, `getEmployeeCount()`, `getDepartmentImage()`, `getTags()` |
| `EmployeeSearchModel` | `Resource`, `SlingHttpServletRequest` | employeesearch | `getPlaceholder()`, `getButtonText()`, `getMaximumResults()`, `getSearchEndpoint()` |
| `EmployeeSpotlightModel` | `Resource` | employeespotlight | `getEmployeeName()`, `getDesignation()`, `getSpotlightText()`, `getInitials()` |
| `FAQModel` | `Resource` | faq | `getFaqItems()` → `List<FAQItemModel>` via `@ChildResource(name="faqItems")` |
| `FAQItemModel` | `Resource` | (child of faq) | `getQuestion()`, `getAnswer()` |
| `TestimonialModel` | `Resource` | testimonial | `getTestimonialItems()` via `@ChildResource(name="testimonialItems")` |
| `TestimonialItemModel` | `Resource` | (child of testimonial) | `getEmployeeName()`, `getDesignation()`, `getFeedback()`, `getPhoto()` |

---

## 11. Components

**Base path:** `ui.apps/src/main/content/jcr_root/apps/employeehub/components/`

### 11.1 Page component

| Property | Value |
|----------|-------|
| resourceType | `employeehub/components/page` |
| Supertype | `core/wcm/components/page/v3/page` |
| Group | `.hidden` |
| HTL files | `body.html`, `customheaderlibs.html`, `customfooterlibs.html` |
| Custom `page.html` | **None** — extends Core Page v3 |

**body.html** — `.eh-page` wrapper + TemplatedContainer:

```html
<div class="eh-page">
    <div class="eh-page-main">
        <sly data-sly-use.templatedContainer="com.day.cq.wcm.foundation.TemplatedContainer"
             data-sly-repeat.child="${templatedContainer.structureResources}"
             data-sly-resource="${child.path @ resourceType=child.resourceType, decorationTagName='div'}"/>
    </div>
</div>
```

**customheaderlibs.html** — loads `employeehub.base,employeehub.components` in all modes.

---

### 11.2 All components (19)

| Component | Group | Supertype | Dialog | EditConfig | Model / Notes |
|-----------|-------|-----------|--------|------------|---------------|
| **page** | `.hidden` | Core Page v3 | inherits | — | TemplatedContainer in body.html |
| **header** | Employee Hub | — | Yes | Yes | Embeds navigation |
| **footer** | Employee Hub | — | Yes | Yes | Copyright, links |
| **navigation** | Employee Hub | — | Yes | Yes | Fallback nav links in HTL |
| **hero** | Employee Hub | — | Yes | Yes | Title, subtitle |
| **breadcrumb** | Employee Hub | — | No | No | Uses Core Page model |
| **title** | Employee Hub | Core Title v3 | Core | Yes | |
| **text** | Employee Hub | Core Text v2 | Core | Yes | |
| **image** | Employee Hub | Core Image v3 | Core | No | |
| **gridcontainer** | Employee Hub - Common | — | No | Yes | `cq:isContainer=true` |
| **contactcards** | Employee Hub | — | No | No | Static HTL |
| **announcement** | Employee Hub | — | Yes | Yes | AnnouncementBannerModel |
| **quicklinks** | Employee Hub | — | Yes | Yes | Multifield links in dialog |
| **employeespotlight** | Employee Hub | — | Yes | Yes | EmployeeSpotlightModel |
| **testimonial** | Employee Hub | — | Yes | Yes | TestimonialModel, multifield |
| **employeecard** | Employee Hub | — | Yes | Yes | EmployeeCardModel |
| **departmentcard** | Employee Hub - Business | — | Yes | Yes | DepartmentCardModel |
| **employeesearch** | Employee Hub | — | Yes | Yes | EmployeeSearchModel; loads clientlib-search |
| **faq** | Employee Hub | — | Yes | Yes | FAQModel, multifield; loads clientlib-faq |

### 11.3 `_cq_editConfig` pattern

Required on every component with a dialog (15 components):

```xml
<jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="nt:unstructured"
    cq:actions="[edit,delete,copymove,insert]"
    cq:dialogMode="floating"
    cq:layout="editbar"/>
```

**Wrong** (ignored by AEM — Open Dialog does nothing):

```xml
<cq:actions jcr:primaryType="nt:unstructured">
    <edit jcr:primaryType="nt:unstructured"/>
</cq:actions>
```

---

## 12. Pages and site structure

**Repo path:** `ui.content/src/main/content/jcr_root/content/employeehub/`

```
/content/employeehub                    ← site root (cq:conf, allowedTemplates)
├── home.html                          ← Landing template
├── employees.html                     ← Content template
├── departments.html                   ← Content template
├── faq.html                           ← Content template
├── contact.html                       ← Content template
└── data/                              ← backend data (not pages)
    ├── employees/
    │   ├── john-doe
    │   ├── sarah-smith
    │   └── mike-johnson
    └── departments/
        ├── it
        ├── hr
        └── finance
```

| Page | Template | Main components (seed content) |
|------|----------|--------------------------------|
| **Home** | employee-landing-template | announcement, quicklinks, employeespotlight, testimonial |
| **Employees** | employee-content-template | title, text, employeesearch, gridcontainer + employeecards |
| **Departments** | employee-content-template | title, gridcontainer + departmentcards |
| **FAQ** | employee-content-template | title, faq (multifield Q&A) |
| **Contact** | employee-content-template | title, text, contactcards |

### Test URLs

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

- [ ] Content Tree shows header, hero/main, footer
- [ ] Open Dialog works on components with `_cq_dialog`
- [ ] Page styled in Edit mode (not only View as Published)
- [ ] Side panel lists policy-allowed components

---

## 13. Data nodes

### Employees

**Path:** `/content/employeehub/data/employees/{node-name}`  
**Node type:** `nt:unstructured`

| Property | Example |
|----------|---------|
| `employeeName` | John Doe |
| `email` | john.doe@abc.com |
| `department` | Information Technology |
| `employeeId` | EMP001 |
| `photo` | DAM path (optional) |
| `cq:tags` | `[employeehub:department/it, employeehub:role/manager, ...]` |

Read by: `EmployeeService` → `EmployeeSearchServlet`

### Departments

**Path:** `/content/employeehub/data/departments/{node-name}`

| Property | Example |
|----------|---------|
| `departmentName` | Information Technology |
| `manager` | John Doe |
| `employeeCount` | 42 |
| `departmentImage` | DAM path (optional) |
| `cq:tags` | department tags |

Read by: `DepartmentService` → `DepartmentSearchServlet`

---

## 14. Tags and DAM

### Tags

**Path:** `/content/cq:tags/employeehub` (created in AEM or via ui.content)

| Namespace | Tags |
|-----------|------|
| `department` | it, hr, finance, marketing, operations |
| `role` | analyst, architect, developer, intern, manager |
| `employment-type` | contract, part-time, permanent |

### DAM

**Path:** `/content/dam/employeehub/`

Placeholder folders: `employees`, `departments`, `documents`, `banners`, `logos`, `icons`

Service user `employeehub-service` has `jcr:read` on this path.

---

## 15. Clientlibs

**Path:** `ui.apps/.../apps/employeehub/clientlibs/`

| Clientlib | Category | Loaded by | Assets |
|-----------|----------|-----------|--------|
| `clientlib-base` | `employeehub.base` | Page header/footer | `css/base.css`, `js/base.js` |
| `clientlib-components` | `employeehub.components` | Page header | `css/components.css` |
| `clientlib-search` | `employeehub.search` | `employeesearch.html` | `js/search.js` |
| `clientlib-faq` | `employeehub.faq` | `faq.html` | `js/faq.js` |

All have `allowProxy=true`.

**CSS scoping:** styles target `.eh-page`, not global `body` or `*`. Clientlibs load in **all WCM modes** including edit.

---

## 16. End-to-end flows

### Page authoring

```
Author opens /editor.html/content/employeehub/home.html
  → AEM loads template policies JSON (must HTTP 200)
  → TemplatedContainer renders structure (header, hero, main, footer)
  → Side panel shows components allowed by policy on main
  → Author clicks wrench → _cq_editConfig + _cq_dialog open Coral dialog
  → HTL + Sling Model render component on publish
```

### Employee search

```
User types in search box on Employees page
  → clientlib-search/js/search.js
  → GET /bin/employeehub/employee-search.json?q=...
  → EmployeeSearchServlet → EmployeeService
  → Reads /content/employeehub/data/employees/*
  → JSON rendered in UI
```

### Announcement banner

```
Component dialog properties (if set) override OSGi defaults
  → AnnouncementBannerModel adapts Resource
  → Falls back to AnnouncementService (OSGi config)
  → Rendered in announcement.html
```

---

## 17. File index (quick reference)

| Area | Source path in repo |
|------|---------------------|
| Templates and policies | `ui.config/src/main/content/jcr_root/conf/employeehub/settings/wcm/` |
| OSGi configs | `ui.config/.../osgiconfig/config/`, `config.author/`, `config.publish/` |
| Java (all) | `core/src/main/java/com/abc/employeehub/core/` |
| Components | `ui.apps/src/main/content/jcr_root/apps/employeehub/components/` |
| Clientlibs | `ui.apps/src/main/content/jcr_root/apps/employeehub/clientlibs/` |
| Pages and data | `ui.content/src/main/content/jcr_root/content/employeehub/` |
| Build narrative | `README.md` |

---

## 18. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Content Tree empty | Template `policies/` not `cq:Page` | Set `jcr:primaryType="cq:Page"` on policies node; redeploy ui.config |
| Open Dialog does nothing | Bad `_cq_editConfig` or missing `_cq_dialog` | Use `cq:actions="[edit,delete,copymove,insert]"` string format |
| Styled in publish only | Clientlibs blocked in edit mode | Load CSS in `customheaderlibs.html` (all modes); scope to `.eh-page` |
| Components not in side panel | Not in template policy allow-list | Update `main` policy `components` attribute |
| Servlet 404 | Core bundle inactive or resolver config missing | Check OSGi bundles + `SlingServletResolver~employeehub` |
| Search returns empty | No employee data nodes | Create nodes under `/content/employeehub/data/employees` |
| Missing `cq:conf` | Site root not linked to conf | Set `cq:conf="/conf/employeehub"` in ui.content |
| Templates not in UI | Wrong node type under `wcm/templates` | Must be `cq:Page` hierarchy |
| OSGi configs missing after deploy | ui.apps filter claims entire `/apps/employeehub` | Narrow ui.apps filter to components/clientlibs/install only |
| Wrong runmode OSGi values | Config in `config/` instead of `config.author` / `config.publish` | Use runmode folders; verify on Author vs Publish ConfigMgr |

---

*Internal use — ABC Corporation*
