# Custom Domain Deployment Guide

This guide covers deploying the Plan My Corpus with your own custom domain. All options below are **free** for the hosting platform itself - you only pay for your domain name (typically $10-15/year).

## Quick Comparison

| Platform | Free Tier | Custom Domain | SSL/HTTPS | Best For |
|----------|-----------|---------------|-----------|----------|
| **Cloudflare Pages** | ✅ Unlimited bandwidth | ✅ Free | ✅ Automatic | Best overall, simplest DNS |
| **Netlify** | ✅ 100GB/month | ✅ Free | ✅ Automatic | Best UX, fastest setup |
| **Vercel** | ✅ 100GB/month | ✅ Free | ✅ Automatic | Great for teams |
| **GitHub Pages** | ✅ 100GB/month | ✅ Free | ✅ Automatic | If already on GitHub |

---

## Option 1: Cloudflare Pages (RECOMMENDED for Custom Domains)

**Why Cloudflare:** If you're using Cloudflare for your domain, this is the absolute easiest option. DNS configuration is instant and automatic.

### Prerequisites
- Cloudflare account (free)
- Domain registered (any registrar)
- Code pushed to GitHub/GitLab

### Step 1: Move Domain to Cloudflare (if not already there)

1. Go to [Cloudflare Dashboard](https://dash.cloudflare.com/)
2. Add Site → Enter your domain
3. Select Free plan
4. Update nameservers at your domain registrar:
   - Cloudflare will provide 2 nameservers (e.g., `ns1.cloudflare.com`, `ns2.cloudflare.com`)
   - Go to your domain registrar (GoDaddy, Namecheap, etc.)
   - Update nameservers to Cloudflare's nameservers
   - Wait 24-48 hours for propagation (usually much faster)

### Step 2: Deploy to Cloudflare Pages

1. **Connect Repository:**
   - Cloudflare Dashboard → Pages → Create a project
   - Connect to Git → Select your repository
   - Authorize Cloudflare

2. **Configure Build:**
   - Framework preset: `None`
   - Build command:
     ```bash
     ./gradlew :composeApp:wasmJsBrowserProductionWebpack && mkdir -p deploy && cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/ && cp composeApp/build/processedResources/wasmJs/main/index.html deploy/
     ```
   - Build output directory: `deploy`
   - Root directory: (leave empty)

3. **Environment Variables:**
   - Add `JAVA_VERSION` = `17`

4. **Deploy** - Click "Save and Deploy"

### Step 3: Add Custom Domain

1. After first deployment completes, go to your project
2. **Custom domains** tab → **Set up a custom domain**
3. Enter your domain (e.g., `financialplanner.com`)
4. Cloudflare automatically:
   - Creates DNS records
   - Provisions SSL certificate
   - Configures HTTPS

**Done!** Your site will be live at your custom domain in ~2 minutes.

### Subdomains

Want to use a subdomain like `app.yourdomain.com`?
- Follow the same steps
- Just enter `app.yourdomain.com` instead of the apex domain
- Cloudflare handles everything automatically

---

## Option 2: Netlify (Best UX, Second Easiest)

**Why Netlify:** Excellent user experience, fast deployment, great documentation.

### Step 1: Deploy to Netlify

1. **Option A: Drag & Drop (Quick)**
   ```bash
   # Build locally
   ./gradlew :composeApp:wasmJsBrowserProductionWebpack
   mkdir -p deploy
   cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/
   cp composeApp/build/processedResources/wasmJs/main/index.html deploy/
   ```
   - Go to [Netlify](https://app.netlify.com/)
   - Drag `deploy/` folder to deploy area

2. **Option B: Git Integration (Automatic deploys)**
   - New site from Git
   - Connect to GitHub
   - Configure build (see Cloudflare section for commands)

### Step 2: Add Custom Domain

1. **Domain Settings** → **Add custom domain**
2. Enter your domain (e.g., `financialplanner.com`)

3. **Configure DNS** (Choose one method):

   **Method A: Use Netlify DNS (Easiest)**
   - Click "Set up Netlify DNS"
   - Update nameservers at your registrar to Netlify's nameservers
   - Wait for propagation (24-48 hours)
   - SSL is automatic

   **Method B: External DNS (Keep current registrar)**
   - Add DNS records at your current provider:
     ```
     Type: A
     Name: @ (or leave empty for apex domain)
     Value: 75.2.60.5

     Type: CNAME
     Name: www
     Value: your-site-name.netlify.app
     ```
   - For subdomains:
     ```
     Type: CNAME
     Name: app (or your subdomain)
     Value: your-site-name.netlify.app
     ```
   - Wait for DNS propagation
   - Click "Verify DNS configuration"
   - SSL certificate provisions automatically

4. **Enable HTTPS** - Automatic after DNS verification

**Done!** Your site is live with HTTPS.

---

## Option 3: Vercel (Great Alternative)

**Why Vercel:** Excellent for teams, great performance, good developer experience.

### Step 1: Deploy to Vercel

1. Go to [Vercel Dashboard](https://vercel.com/dashboard)
2. **Import Project** → Select your GitHub repository
3. Configure:
   - Framework Preset: `Other`
   - Build Command:
     ```bash
     ./gradlew :composeApp:wasmJsBrowserProductionWebpack && mkdir -p deploy && cp composeApp/build/kotlin-webpack/wasmJs/productionExecutable/* deploy/ && cp composeApp/build/processedResources/wasmJs/main/index.html deploy/
     ```
   - Output Directory: `deploy`

4. **Deploy**

### Step 2: Add Custom Domain

1. **Project Settings** → **Domains**
2. Enter your domain
3. Choose configuration:

   **For Apex Domain (yourdomain.com):**
   ```
   Type: A
   Name: @
   Value: 76.76.21.21

   Type: CNAME
   Name: www
   Value: cname.vercel-dns.com
   ```

   **For Subdomain (app.yourdomain.com):**
   ```
   Type: CNAME
   Name: app
   Value: cname.vercel-dns.com
   ```

4. Add records at your DNS provider
5. Wait for verification (~5-10 minutes)
6. SSL is automatic

**Done!**

---

## Option 4: GitHub Pages with Custom Domain

**Why GitHub Pages:** Simple if code is already on GitHub, good for open-source projects.

### Step 1: Deploy to GitHub Pages

See main [DEPLOYMENT.md](DEPLOYMENT.md) for build and deployment steps.

### Step 2: Add Custom Domain

1. **Repository Settings** → **Pages** → **Custom domain**
2. Enter your domain

3. **Configure DNS at your provider:**

   **For Apex Domain (yourdomain.com):**
   ```
   Type: A
   Name: @
   Value: 185.199.108.153

   Type: A
   Name: @
   Value: 185.199.109.153

   Type: A
   Name: @
   Value: 185.199.110.153

   Type: A
   Name: @
   Value: 185.199.111.153

   Type: CNAME
   Name: www
   Value: YOUR_USERNAME.github.io
   ```

   **For Subdomain (app.yourdomain.com):**
   ```
   Type: CNAME
   Name: app
   Value: YOUR_USERNAME.github.io
   ```

4. **Add CNAME file** to your repository:
   ```bash
   echo "yourdomain.com" > CNAME
   # Include this in your deploy/ directory before pushing to gh-pages
   ```

5. **Enable HTTPS** - Check "Enforce HTTPS" in repository settings after DNS propagates

**Done!** Wait 24-48 hours for full propagation.

---

## Domain Registrars - Where to Buy

### Recommended Registrars

1. **Cloudflare Registrar** (At-cost pricing, no markup)
   - Most affordable
   - Free WHOIS privacy
   - Instant integration with Cloudflare Pages
   - ~$9-10/year for .com

2. **Namecheap** (Good value)
   - ~$13-15/year for .com
   - Free WHOIS privacy
   - Easy DNS management
   - Frequent promos

3. **Google Domains → Squarespace** (Recently acquired)
   - ~$12/year for .com
   - Clean interface
   - Good support

4. **Porkbun** (Budget-friendly)
   - ~$10/year for .com
   - Free WHOIS privacy
   - Good for developers

### Avoid
- GoDaddy (expensive renewals, aggressive upselling)
- Domain.com (expensive, poor UX)

---

## DNS Propagation Time

- **Cloudflare Pages:** Almost instant (minutes) if using Cloudflare DNS
- **Netlify:** 2-24 hours with external DNS, instant with Netlify DNS
- **Vercel:** 5 minutes - 24 hours
- **GitHub Pages:** 24-48 hours

**Pro Tip:** Use [whatsmydns.net](https://www.whatsmydns.net/) to check DNS propagation worldwide.

---

## SSL/HTTPS Certificates

All platforms provide **free, automatic SSL certificates** via Let's Encrypt:
- Auto-renewal every 90 days
- No configuration needed
- Wildcard certificates for subdomains
- Zero cost

---

## Subdomain vs Apex Domain

### Apex Domain (yourdomain.com)
- **Pros:** Professional, memorable
- **Cons:** DNS setup slightly more complex
- **Use:** Production app, main product

### Subdomain (app.yourdomain.com)
- **Pros:** Easier DNS setup (just CNAME), can separate services
- **Cons:** Slightly longer URL
- **Use:** SaaS applications, separate app from marketing site

### Both (Recommended)
Set up both and redirect:
- `yourdomain.com` → Primary
- `www.yourdomain.com` → Redirects to apex
- All platforms support this automatically

---

## Cost Breakdown

| Item | Cost | Frequency |
|------|------|-----------|
| Domain (.com) | $10-15 | Yearly |
| Hosting | $0 | Free forever |
| SSL Certificate | $0 | Free (auto-renewed) |
| Bandwidth | $0 | Free (up to 100GB or unlimited) |
| **Total** | **$10-15** | **Per year** |

---

## Recommended Setup (Best Overall)

1. **Buy domain:** Cloudflare Registrar (~$10/year)
2. **Deploy to:** Cloudflare Pages (unlimited bandwidth)
3. **Configure domain:** Automatic (2 minutes)
4. **SSL:** Automatic
5. **Total time:** 30 minutes
6. **Total cost:** $10/year

### Why This Combo?
- ✅ Cheapest domain pricing
- ✅ Unlimited bandwidth
- ✅ Fastest deployment
- ✅ Instant DNS updates
- ✅ Best performance (Cloudflare CDN)
- ✅ Simplest configuration
- ✅ Free email forwarding (bonus)

---

## Migration Between Platforms

Already deployed elsewhere and want to switch?

1. **Keep current deployment running**
2. Deploy to new platform
3. Test using platform's default URL (e.g., `yoursite.netlify.app`)
4. Once confirmed working, update DNS to point to new platform
5. Old deployment stays as backup

No downtime needed!

---

## Troubleshooting

### Domain Not Resolving
- Check DNS propagation: [whatsmydns.net](https://www.whatsmydns.net/)
- Wait 24-48 hours for full propagation
- Verify DNS records are correct
- Clear browser cache: `Ctrl+Shift+Del` (or `Cmd+Shift+Del` on Mac)

### SSL Certificate Not Provisioning
- Ensure DNS is fully propagated first
- Some platforms require HTTP validation - ensure site is accessible via HTTP first
- Can take up to 24 hours after DNS propagation

### Site Shows "Domain Not Configured"
- DNS records pointing to wrong place
- Double-check A/CNAME records match platform requirements
- Platform may need 5-10 minutes to detect new domain

### Custom Domain Shows Different Content
- Clear CloudFlare cache (if using Cloudflare)
- Clear browser cache
- Check deployment status on platform dashboard

---

## Next Steps After Deployment

1. **Test thoroughly:**
   - All screens load
   - LocalStorage works
   - Export/Import works
   - Charts render correctly

2. **Set up monitoring:** (Optional)
   - [UptimeRobot](https://uptimerobot.com/) - Free uptime monitoring
   - [Google Analytics](https://analytics.google.com/) - Free usage tracking

3. **Update Contact Info:**
   - Edit `AboutScreen.kt` with your actual contact information
   - Rebuild and redeploy

4. **Share your app:**
   - Add to your portfolio
   - Share on social media
   - Gather user feedback

---

## Support & Questions

If you encounter issues:
1. Check platform's status page (all have them)
2. Review platform documentation
3. Check community forums (Netlify, Vercel, Cloudflare all have active communities)
4. Contact platform support (all offer free support for basic issues)

---

**You're ready to deploy with your custom domain! 🚀**

Choose your platform, follow the steps, and your Plan My Corpus will be live at your custom domain within an hour.
