# Deployment Guide - Plan My Corpus

This guide provides step-by-step instructions for deploying the Plan My Corpus application to production.

## Prerequisites

- JDK 17 or higher
- Git (for GitHub-based deployments)
- Gradle (included via wrapper)

## Building for Production

### Step 1: Run Production Build

```bash
./gradlew :composeApp:wasmJsBrowserProductionWebpack
```

**Build Output:**
- Duration: ~1-2 minutes (first build may take longer)
- Output directory: `composeApp/build/kotlin-webpack/wasmJs/productionExecutable/`
- Generated files:
  - `composeApp.js` (~532 KB) - Main JavaScript bundle
  - `*.wasm` (~7.9 MB) - WebAssembly binary
  - `index.html` - Entry HTML file (located at `composeApp/build/processedResources/wasmJs/main/`)

### Step 2: Prepare Deployment Package

You need to copy two sets of files to your deployment directory:

```bash
# Create deployment directory
mkdir -p deploy

# Copy JavaScript and WASM files
cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/

# Copy index.html
cp composeApp/build/processedResources/wasmJs/main/index.html deploy/
```

Your `deploy/` directory should now contain:
- `index.html`
- `composeApp.js`
- `*.wasm` (WebAssembly binary)
- `composeApp.js.map` (source map, optional for production)

## Deployment Options

Choose one of the following free hosting options:

---

## Option 1: GitHub Pages (Recommended)

**Cost:** FREE
**Bandwidth:** 100 GB/month
**Build Time:** 2-5 minutes

### Manual Deployment

1. **Initialize Git repository (if not already done):**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   ```

2. **Create GitHub repository** and push code:
   ```bash
   git remote add origin https://github.com/YOUR_USERNAME/financial-planner.git
   git branch -M main
   git push -u origin main
   ```

3. **Build and deploy:**
   ```bash
   # Build production bundle
   ./gradlew :composeApp:wasmJsBrowserProductionWebpack

   # Create deployment package
   mkdir -p deploy
   cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/
   cp composeApp/build/processedResources/wasmJs/main/index.html deploy/

   # Deploy to gh-pages branch
   cd deploy
   git init
   git add .
   git commit -m "Deploy to GitHub Pages"
   git push -f https://github.com/YOUR_USERNAME/financial-planner.git main:gh-pages
   ```

4. **Enable GitHub Pages:**
   - Go to repository Settings > Pages
   - Source: Deploy from a branch
   - Branch: `gh-pages` / `root`
   - Click Save

5. **Access your site:**
   - URL: `https://YOUR_USERNAME.github.io/financial-planner/`
   - Allow 2-5 minutes for first deployment

### Automated Deployment with GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to GitHub Pages

on:
  push:
    branches: [ main ]
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Setup JDK 17
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build with Gradle
        run: ./gradlew :composeApp:wasmJsBrowserProductionWebpack

      - name: Prepare deployment
        run: |
          mkdir -p deploy
          cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/
          cp composeApp/build/processedResources/wasmJs/main/index.html deploy/

      - name: Upload artifact
        uses: actions/upload-pages-artifact@v2
        with:
          path: deploy

      - name: Deploy to GitHub Pages
        id: deployment
        uses: actions/deploy-pages@v2
```

---

## Option 2: Netlify

**Cost:** FREE
**Bandwidth:** 100 GB/month
**Build Minutes:** 300 minutes/month

### Drag-and-Drop Deployment

1. Build locally:
   ```bash
   ./gradlew :composeApp:wasmJsBrowserProductionWebpack
   mkdir -p deploy
   cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/
   cp composeApp/build/processedResources/wasmJs/main/index.html deploy/
   ```

2. Go to [Netlify](https://app.netlify.com/)
3. Drag the `deploy/` folder to the deployment area
4. Done! Your site is live

### Automated Deployment

1. **Create `netlify.toml` in project root:**
   ```toml
   [build]
     command = "./gradlew :composeApp:wasmJsBrowserProductionWebpack && mkdir -p deploy && cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/ && cp composeApp/build/processedResources/wasmJs/main/index.html deploy/"
     publish = "deploy"

   [[redirects]]
     from = "/*"
     to = "/index.html"
     status = 200
   ```

2. **Connect to GitHub:**
   - Go to Netlify Dashboard
   - New site from Git
   - Connect to GitHub repository
   - Site settings will be auto-detected from `netlify.toml`
   - Deploy site

3. **Configure custom domain (optional):**
   - Domain settings > Add custom domain
   - Follow DNS configuration instructions

---

## Option 3: Cloudflare Pages

**Cost:** FREE
**Bandwidth:** UNLIMITED
**Builds:** 500/month

### Deployment Steps

1. **Push code to GitHub** (if not already done)

2. **Connect to Cloudflare Pages:**
   - Go to [Cloudflare Dashboard](https://dash.cloudflare.com/) > Pages
   - Create a project > Connect to Git
   - Select your repository

3. **Configure build settings:**
   - Framework preset: None
   - Build command:
     ```bash
     ./gradlew :composeApp:wasmJsBrowserProductionWebpack && mkdir -p deploy && cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/ && cp composeApp/build/processedResources/wasmJs/main/index.html deploy/
     ```
   - Build output directory: `deploy`
   - Root directory: (leave empty)

4. **Environment variables:**
   - Add `JAVA_VERSION` = `17`

5. **Deploy**

---

## Option 4: Vercel

**Cost:** FREE
**Bandwidth:** 100 GB/month

### Deployment Steps

1. **Push code to GitHub**

2. **Import to Vercel:**
   - Go to [Vercel Dashboard](https://vercel.com/dashboard)
   - New Project > Import Git Repository
   - Select your repository

3. **Configure:**
   - Framework Preset: Other
   - Build Command:
     ```bash
     ./gradlew :composeApp:wasmJsBrowserProductionWebpack && mkdir -p deploy && cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/ && cp composeApp/build/processedResources/wasmJs/main/index.html deploy/
     ```
   - Output Directory: `deploy`

4. **Deploy**

---

## Post-Deployment Checklist

After deploying, verify the following:

- [ ] App loads without errors
- [ ] All screens are accessible (Dashboard, Portfolio, Goals, Analysis, Settings, About)
- [ ] Export/Import functionality works
- [ ] LocalStorage persistence works
- [ ] Charts and visualizations render correctly
- [ ] About/Disclaimer pages display correctly
- [ ] Mobile responsiveness (test on different screen sizes)
- [ ] Browser compatibility (test on Chrome, Firefox, Safari, Edge)

## Custom Domain Configuration

### GitHub Pages
1. Add `CNAME` file to `deploy/` directory with your domain
2. Configure DNS:
   - Type: `A` Record
   - Name: `@`
   - Value: GitHub Pages IPs (check current IPs in GitHub docs)
3. Enable HTTPS in repository settings

### Netlify/Vercel/Cloudflare
Follow the platform-specific instructions in their dashboard for custom domain configuration.

## Troubleshooting

### Build Fails
- Ensure JDK 17+ is installed
- Check Gradle version: `./gradlew --version`
- Clean build: `./gradlew clean`
- Try again: `./gradlew :composeApp:wasmJsBrowserProductionWebpack`

### Files Not Found After Deployment
- Verify all files were copied to deployment directory
- Check that `index.html` is in the root of deployment directory
- Ensure WASM file is accessible (check browser console for 404 errors)

### App Loads But Doesn't Work
- Check browser console for JavaScript errors
- Verify localStorage is enabled in browser
- Test in incognito mode to rule out extension conflicts

### Large Bundle Size Warning
- This is expected for WebAssembly applications
- Bundle size: ~8.4 MB total (532 KB JS + 7.9 MB WASM)
- WASM compresses well with gzip (typically 50-70% size reduction)
- Most hosting platforms automatically enable compression

## Performance Optimization

- **Enable Compression:** Most hosts do this automatically. Verify gzip/brotli is enabled.
- **CDN:** All recommended hosts include global CDN automatically.
- **Caching:** Configure browser caching headers (if host allows):
  ```
  Cache-Control: public, max-age=31536000, immutable
  ```

## Monitoring

### Free Monitoring Options
- **Google Analytics** - Add tracking code to `index.html`
- **Plausible Analytics** - Privacy-friendly alternative
- **Netlify Analytics** - Built-in (if using Netlify)

## Contact Information

**Before Deployment:**
- Update contact information in `AboutScreen.kt`
- Replace placeholder text:
  ```kotlin
  Text(
      text = "GitHub: [Your GitHub URL]\nEmail: [Your Email]",
      // ...
  )
  ```

## Security Considerations

- All data is stored client-side in localStorage
- No backend or API calls
- No user authentication required
- No sensitive data transmitted over network
- HTTPS is strongly recommended (enabled by default on all platforms)

## Maintenance

### Updating the App
1. Make code changes
2. Test locally: `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`
3. Build: `./gradlew :composeApp:wasmJsBrowserProductionWebpack`
4. Deploy using your chosen method (automatic if CI/CD is set up)

### Backup User Data
Since data is stored in browser localStorage:
- Users should regularly export their data (Export Snapshot feature)
- Consider adding a reminder/prompt for users to backup
- Document the export/import feature prominently

## Cost Estimates

All recommended options are **100% FREE** for typical usage:

| Platform | Free Tier | Best For |
|----------|-----------|----------|
| GitHub Pages | 100 GB/month bandwidth | Simple deployments, already using GitHub |
| Netlify | 100 GB/month + 300 build minutes | Best DX, automatic deploys |
| Cloudflare Pages | **Unlimited** bandwidth | High traffic, best performance |
| Vercel | 100 GB/month | Vercel ecosystem users |

**Recommendation:** Start with GitHub Pages or Netlify. Migrate to Cloudflare Pages if traffic grows significantly.
