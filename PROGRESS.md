# ABC Employee Hub — Build Checklist

Step-by-step checklist for this project at **`/Users/<user>/Desktop/employeehub`**.

Use to **build from scratch**, **redeploy after changes**, or **verify** a fresh AEM install.

> **Read [README.md](./README.md)** for narrative guide and [DOCUMENTATION.md](./DOCUMENTATION.md) for architecture reference.

| Setting | Value |
|---------|-------|
| **Project folder** | `/Users/<user>/Desktop/employeehub` |
| **AEM URL** | http://localhost:4502 |
| **AEM login** | `admin` / `admin` |
| **appId** | `employeehub` |
| **groupId** | `com.abc.employeehub` |
| **JDK** | 11+ (21 OK with 2025 SDK runtime) |
| **Maven compile target** | Java 11 (in pom.xml) |

---

## How to use this file

- Check off each box `[ ]` → `[x]` as you complete steps.
- **🔧** = edit/create a file in this project
- **🚀** = run Maven deploy
- **🌐** = step in AEM UI / browser
- **✅** = verification gate — do not skip

If the project is **already built**, start at **Phase 13** to verify authoring, or deploy from the phase you changed.

---

## Critical authoring rules (do not skip)

These caused **empty Content Tree**, **dialogs that don’t open**, and **missing styles in edit mode** before they were fixed.

### Page component

| Do | Don’t |
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

- Set `cq:conf="/conf/employeehub"` on `/content/employeehub` (in ui.content).

### `_cq_editConfig` (every dialog component)

```xml
<jcr:root xmlns:cq="http://www.day.com/jcr/cq/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="nt:unstructured"
    cq:actions="[edit,delete,copymove,insert]"
    cq:dialogMode="floating"
    cq:layout="editbar"/>
```

Components with dialogs also need `_cq_dialog/`. Without both, **Open Dialog** does nothing.

---

## Deploy commands

Full package (ui.apps + ui.config — **not** ui.content):

```bash
cd /Users/<user>/Desktop/employeehub
mvn clean install -PautoInstallSinglePackage \
  -Daem.host=localhost \
  -Daem.port=4502 \
  -Daem.user=admin \
  -Daem.password=admin
```

ui.config only:

```bash
mvn clean install -pl ui.config -PautoInstallSinglePackage -Daem.port=4502
```

Publish (optional — same ui.config package, publish run mode picks `config.publish/`):

```bash
mvn clean install -pl ui.config -PautoInstallSinglePackage -Daem.port=4503
```

ui.content only (install separately after building):

```bash
cd ui.content
mvn clean install
mvn com.day.jcr.vault:content-package-maven-plugin:1.0.4:install \
  -Dvault.file=target/employeehub.ui.content-1.0.0-SNAPSHOT.zip \
  -Daem.host=localhost -Daem.port=4502 \
  -Daem.user=admin -Daem.password=admin
```

Build only (no AEM):

```bash
mvn clean install
```

---

## Test URLs (after Phase 13)

| Page | URL |
|------|-----|
| Home (editor) | http://localhost:4502/editor.html/content/employeehub/home.html |
| Home (publish view) | http://localhost:4502/content/employeehub/home.html?wcmmode=disabled |
| Employees | http://localhost:4502/content/employeehub/employees.html |
| Departments | http://localhost:4502/content/employeehub/departments.html |
| FAQ | http://localhost:4502/content/employeehub/faq.html |
| Contact | http://localhost:4502/content/employeehub/contact.html |
| Templates | http://localhost:4502/libs/wcm/core/content/sites/templates.html/conf/employeehub |

Servlet tests:

```bash
curl -u admin:admin "http://localhost:4502/bin/employeehub/employee-search.json?q=john"
curl -u admin:admin "http://localhost:4502/bin/employeehub/department-search.json"
curl -u admin:admin "http://localhost:4502/bin/employeehub/announcement.json"
```

Design JSON (Content Tree dependency — expect **200**):

```bash
curl -u admin:admin -o /dev/null -w "%{http_code}\n" \
  "http://localhost:4502/conf/employeehub/settings/wcm/templates/employee-landing-template/policies/_jcr_content.1500560231670.json"
```

---

# PHASE 0 — Environment (Steps 1–26)

### AEM instance

- [ ] 1. Start AEM Author SDK on port **4502**
- [ ] 2. ✅ Open http://localhost:4502 — login page loads
- [ ] 3. ✅ Log in as admin / admin
- [ ] 4. Bookmark Bundles: http://localhost:4502/system/console/bundles
- [ ] 5. Bookmark ConfigMgr: http://localhost:4502/system/console/configMgr
- [ ] 6. Bookmark Package Manager: http://localhost:4502/crx/packmgr
- [ ] 7. Bookmark CRX DE: http://localhost:4502/crx/de/index.jsp

### Maven + SDK API

- [ ] 8. Verify Maven: `mvn -version`
- [ ] 9. Locate `aem-sdk-api-*.jar` inside your SDK zip
- [ ] 10. Install API to local Maven repo if not already done (see README Phase 0)
- [ ] 11. ✅ BUILD SUCCESS for install-file
- [ ] 12. Open `/Users/<user>/Desktop/employeehub` in your IDE
- [ ] 13. Confirm modules: core, ui.apps, ui.content, ui.config, all
- [ ] 14. ✅ AEM still running on port 4502
- [ ] 15. ✅ Phase 0 complete

---

# PHASE 1 — Maven Archetype Scaffold (Steps 16–34)

*Skip if project already exists — verify pom.xml settings instead.*

- [ ] 16. Confirm parent `pom.xml` has correct `<aem.sdk.api>` version
- [ ] 17. 🔧 Confirm `<aem.port>4502</aem.port>`
- [ ] 18. 🔧 Confirm `<aem.host>localhost</aem.host>`
- [ ] 19. First build: `mvn clean install` (no deploy)
- [ ] 20. ✅ BUILD SUCCESS all modules
- [ ] 21. ✅ Phase 1 complete

---

# PHASE 2 — Project Wiring (Steps 35–60)

### Vault filters

- [ ] 35. 🔧 ui.apps filters: `components`, `clientlibs`, `install` only (**not** entire `/apps/employeehub` — avoids wiping osgiconfig)
- [ ] 36. 🔧 ui.content filters: `/content/employeehub`, `/content/dam/employeehub`, `/content/cq:tags/employeehub`
- [ ] 37. 🔧 ui.config filters: `/apps/employeehub/osgiconfig`, `/conf/employeehub`

### Maven wiring

- [ ] 38. 🔧 ui.apps embeds core bundle at `/apps/employeehub/install`
- [ ] 39. 🔧 Parent pom has `autoInstallSinglePackage` profile
- [ ] 40. 🔧 all/pom.xml: container, sub-packages ui.apps + ui.config only (NOT ui.content)
- [ ] 41. 🔧 all/pom.xml: `skipValidation=true`
- [ ] 42. 🔧 core/pom.xml: Export-Package + Sling-Model-Packages
- [ ] 43. `mvn clean install` (no profile) → ✅ BUILD SUCCESS
- [ ] 44. ✅ Phase 2 complete

---

# PHASE 3 — ui.config OSGi Foundation (Steps 61–74)

**Folder:** `ui.config/src/main/content/jcr_root/apps/employeehub/osgiconfig/`

### Shared configs (`config/` — all run modes)

- [ ] 61. 🔧 `config/RepositoryInitializer~employeehub.cfg.json`
- [ ] 62. 🔧 `config/ServiceUserMapperImpl.amended~employeehub.cfg.json`
- [ ] 63. 🔧 `config/SlingServletResolver~employeehub.cfg.json`

### Author run mode (`config.author/`)

- [ ] 64. 🔧 `config.author/EmployeeHubConfiguration.cfg.json` — `environmentName=author`, `maximumSearchResults=25`
- [ ] 65. 🔧 `config.author/AnnouncementConfiguration.cfg.json` — preview message, `#0066cc`

### Publish run mode (`config.publish/`)

- [ ] 66. 🔧 `config.publish/EmployeeHubConfiguration.cfg.json` — `environmentName=publish`, `maximumSearchResults=15`
- [ ] 67. 🔧 `config.publish/AnnouncementConfiguration.cfg.json` — production message, `#004499`

- [ ] 68. 🚀 Deploy: `mvn clean install -pl ui.config -PautoInstallSinglePackage -Daem.port=4502`
- [ ] 69. ✅ BUILD SUCCESS + package installed
- [ ] 70. 🌐 ConfigMgr → Employee Hub configs show **author** values on `:4502`
- [ ] 71. Optional: deploy same package to Publish `:4503` → verify **publish** values
- [ ] 72. ✅ Phase 3 complete

---

# PHASE 4 — Core Module (Steps 73–105)

**Base:** `core/src/main/java/com/abc/employeehub/core/`

- [ ] 73. 🔧 Configuration interfaces (EmployeeHub, Announcement)
- [ ] 74. 🔧 Services + impls (Employee, Department, Announcement, FAQ)
- [ ] 75. 🔧 Sling models (9 models)
- [ ] 76. 🔧 Servlets (employee-search, department-search, announcement)
- [ ] 77. `mvn clean install -pl core` → ✅ compiles
- [ ] 78. 🚀 Full deploy: `mvn clean install -PautoInstallSinglePackage -Daem.port=4502`
- [ ] 79. 🌐 Bundle `com.abc.employeehub.core` = **Active**
- [ ] 80. Test announcement servlet → ✅ JSON response
- [ ] 81. ✅ Phase 4 complete

---

# PHASE 5 — Page Component + Base Clientlibs (Steps 106–124)

### Page component (Core WCM Page v3 — **no** custom `page.html`)

- [ ] 106. 🔧 `components/page/.content.xml` — supertype Core Page v3
- [ ] 107. ✅ Confirm **no** `components/page/page.html`
- [ ] 108. 🔧 `components/page/body.html` — TemplatedContainer + `.eh-page`
- [ ] 109. 🔧 `components/page/customheaderlibs.html` — CSS all modes
- [ ] 110. 🔧 `components/page/customfooterlibs.html` — JS all modes

### Clientlibs

- [ ] 111. 🔧 `clientlib-base` — category `[employeehub.base]`, scoped CSS
- [ ] 112. 🔧 `clientlib-components` — category `[employeehub.components]`
- [ ] 113. 🚀 Deploy full package
- [ ] 114. CRX DE → page component exists, no page.html ✅
- [ ] 115. ✅ Phase 5 complete

---

# PHASE 6 — Structure Components (Steps 125–137)

- [ ] 125. 🔧 **navigation** — HTL, `_cq_dialog`, `_cq_editConfig`
- [ ] 126. 🔧 **header** — HTL, `_cq_dialog`, `_cq_editConfig`
- [ ] 127. 🔧 **footer** — HTL, `_cq_dialog`, `_cq_editConfig`
- [ ] 128. 🔧 **breadcrumb** — HTL
- [ ] 129. 🔧 **hero** — HTL, `_cq_dialog`, `_cq_editConfig`
- [ ] 130. 🚀 Deploy
- [ ] 131. CRX DE → `_cq_editConfig` + `_cq_dialog` on nav/header/footer/hero
- [ ] 132. ✅ Phase 6 complete

---

# PHASE 7 — Editable Templates + Policies (Steps 138–159)

**Folder:** `ui.config/.../conf/employeehub/settings/wcm/`

- [ ] 138. 🔧 Conf hierarchy — `wcm/` and `templates/` as **cq:Page**
- [ ] 139. 🔧 `template-types/page/` — structure, initial, policies
- [ ] 140. 🔧 **employee-landing-template** — structure uses `employeehub/components/page`
- [ ] 141. 🔧 Landing `policies/` — **cq:Page** + embedded policy on `main`
- [ ] 142. 🔧 **employee-content-template** — same pattern
- [ ] 143. 🔧 Content `policies/` — **cq:Page** + gridcontainer nested policy
- [ ] 144. 🔧 Both templates have `cq:templateType` set
- [ ] 145. 🚀 Deploy full package
- [ ] 146. ✅ Design JSON curl returns **200**
- [ ] 147. 🌐 Both templates visible and enabled in template console
- [ ] 148. ✅ Phase 7 complete

---

# PHASE 8 — Common Components (Steps 160–169)

- [ ] 160. 🔧 **title** — Core supertype, `_cq_editConfig`
- [ ] 161. 🔧 **text** — Core supertype, `_cq_editConfig`
- [ ] 162. 🔧 **image** — Core supertype
- [ ] 163. 🔧 **gridcontainer** — `_cq_editConfig`
- [ ] 164. 🔧 **contactcards**
- [ ] 165. 🚀 Deploy
- [ ] 166. ✅ Phase 8 complete

---

# PHASE 9 — Business Components (Steps 170–183)

- [ ] 170. 🔧 **announcement** — dialog, editConfig, model
- [ ] 171. 🔧 **employeecard** — dialog, editConfig, model
- [ ] 172. 🔧 **departmentcard** — dialog, editConfig, model
- [ ] 173. 🔧 **employeesearch** — dialog, editConfig, model
- [ ] 174. 🔧 **employeespotlight** — dialog, editConfig
- [ ] 175. 🔧 **quicklinks** — dialog, editConfig
- [ ] 176. 🔧 **faq** — dialog (multifield), editConfig, model
- [ ] 177. 🔧 **testimonial** — dialog (multifield), editConfig, model
- [ ] 178. 🚀 Deploy full package
- [ ] 179. CRX DE → every dialog component has `_cq_dialog` + `_cq_editConfig`
- [ ] 180. ✅ Phase 9 complete

---

# PHASE 10 — Feature Clientlibs (Steps 184–192)

- [ ] 184. 🔧 `clientlib-search` + include in `employeesearch.html`
- [ ] 185. 🔧 `clientlib-faq` + include in `faq.html`
- [ ] 186. 🚀 Deploy
- [ ] 187. ✅ Phase 10 complete

---

# PHASE 11 — ui.content Package (Steps 193–204)

- [ ] 193. 🔧 Site root has `cq:conf="/conf/employeehub"`
- [ ] 194. 🔧 Pages: home, employees, departments, faq, contact
- [ ] 195. 🔧 Data nodes under `/content/employeehub/data/`
- [ ] 196. `mvn clean install -pl ui.content` → ✅ BUILD SUCCESS
- [ ] 197. 🚀 Install ui.content zip (see Deploy commands)
- [ ] 198. CRX DE → site + pages + data exist
- [ ] 199. ✅ Phase 11 complete

---

# PHASE 12 — Tags & Optional Content (Steps 205–212)

- [ ] 205. 🌐 Tags namespace `employeehub` (department, role, employment-type)
- [ ] 206. Optional: DAM assets under `/content/dam/employeehub/`
- [ ] 207. Optional: extra components on pages in Edit mode
- [ ] 208. ✅ Phase 12 complete

---

# PHASE 13 — Final Verification (Steps 213–230)

### Build & runtime

- [ ] 213. 🚀 Final deploy: `mvn clean install -PautoInstallSinglePackage -Daem.port=4502`
- [ ] 214. 🚀 Re-install ui.content if content changed
- [ ] 215. ✅ BUILD SUCCESS
- [ ] 216. 🌐 Bundle `employeehub.core` = Active
- [ ] 217. ✅ Design JSON → HTTP **200**
- [ ] 218. ✅ All pages → HTTP 200
- [ ] 219. ✅ All servlets return JSON

### Authoring

- [ ] 220. 🌐 **Content Tree** not empty on Home editor
- [ ] 221. 🌐 **Open Dialog** works on announcement, quicklinks, hero, etc.
- [ ] 222. 🌐 Page **styled in Edit mode**
- [ ] 223. 🌐 Side panel lists policy-allowed components
- [ ] 224. 🌐 Templates enabled in template console

### Feature smoke tests

- [ ] 225. Live search on Employees — type "john" → results
- [ ] 226. FAQ accordion works
- [ ] 227. Publish view: `?wcmmode=disabled`
- [ ] 228. Optional: compare with WKND editor on same instance
- [ ] 229. Optional: init git repo
- [ ] 230. ✅ **PROJECT COMPLETE**

---

# Build Order Summary

```
Archetype / verify pom → ui.config (OSGi) → core → page + clientlibs
→ structure components → templates + policies + template-types
→ common components → business components → feature clientlibs
→ ui.content (separate install) → tags → final verification
```

---

# Troubleshooting

| Issue | Fix |
|-------|-----|
| Content Tree empty | Template `policies/` as **cq:Page**; design JSON must return 200 |
| Open Dialog broken | `cq:actions="[edit,delete,copymove,insert]"` + `_cq_dialog` |
| Styled in publish only | Clientlibs in all modes; scope CSS to `.eh-page` |
| Servlet 404 | Bundle Active + SlingServletResolver config |
| Templates not in UI | Deploy ui.config; `wcm/templates` = cq:Page; add template-types |
| Missing cq:conf | Set on `/content/employeehub` in ui.content |
| OSGi configs missing / wrong runmode values | ui.apps filter owns full `/apps/employeehub` | Narrow ui.apps filters; osgiconfig only in ui.config |

See [DOCUMENTATION.md](./DOCUMENTATION.md) and [README.md](./README.md) for full detail.

---

# Reference — All Components (19)

| Component | Dialog | EditConfig | Model |
|-----------|--------|------------|-------|
| page | — | — | Core Page v3 |
| header, footer, navigation, hero | Yes | Yes | — |
| title, text | Core | Yes | — |
| gridcontainer | — | Yes | — |
| announcement, employeecard, departmentcard, employeesearch | Yes | Yes | Yes |
| faq, testimonial | Yes | Yes | Yes |
| quicklinks, employeespotlight | Yes | Yes | — |
| breadcrumb, image, contactcards | — | — | — |

---

# Notes / Session Log

```
Date started: _______________
SDK version:  _______________
AEM port:     4502
Issues:       _______________
Completed:    _______________
```

---

*Internal use — ABC Corporation*
