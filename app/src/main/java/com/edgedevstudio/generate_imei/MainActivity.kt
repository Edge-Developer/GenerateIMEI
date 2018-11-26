package com.edgedevstudio.generate_imei

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.support.constraint.ConstraintLayout
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ShareCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import com.codemybrainsout.ratingdialog.RatingDialog
import com.edgedevstudio.generate_imei.Tabs.Tab1
import com.edgedevstudio.generate_imei.Tabs.Tab2
import com.edgedevstudio.generate_imei.Tabs.Tab3
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.firebase.analytics.FirebaseAnalytics

import java.util.Locale

import io.github.yavski.fabspeeddial.FabSpeedDial
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.*
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.design.widget.Snackbar.LENGTH_LONG
import android.support.design.widget.Snackbar.LENGTH_SHORT
import android.support.design.widget.Snackbar.make
import com.edgedevstudio.generate_imei.LanguageActivity.LANG_COUNTRY_CODE_KEY
import com.edgedevstudio.generate_imei.LanguageActivity.LANG_SHORTCODE_KEY
import com.edgedevstudio.generate_imei.R.id.*

class MainActivity : AppCompatActivity(), Tab1.Callback, Tab2.Callback, Tab3.Callback, NavigationView.OnNavigationItemSelectedListener {
    private var mPagerAdapter: MyPagerAdapter? = null
    private lateinit var drawer: DrawerLayout
    private lateinit var mTabLayout: TabLayout
    private lateinit var mViewPager: ViewPager
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var mFabSpeedDial: FabSpeedDial
    private lateinit var toggle: ActionBarDrawerToggle
    private var tabPosition: Int = 0

    private lateinit var mAdView: AdView
    private lateinit var mConstraintLayout: ConstraintLayout
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private var storagePermissionStatus: TinyDB? = null
    private val mTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            tabPosition = tab.position
            val charSequence = tab.contentDescription ?: return
            val tabName = charSequence.toString()
            mFirebaseAnalytics.setCurrentScreen(this@MainActivity, tabName, null)
            displayAd()
            mViewPager.currentItem = tab.position
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {

        }

        override fun onTabReselected(tab: TabLayout.Tab) {

        }
    }
    private var sentToSettings = false
    private val fabMenuListener = object : SimpleMenuListenerAdapter() {

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            val itemId = menuItem.itemId
            displayAd()
            val fragment = mPagerAdapter!!.getFragment(mTabLayout.selectedTabPosition)
                    ?: return false
            if (itemId == R.id.action_share || itemId == R.id.action_copy) {
                var shareIMEI: String? = null
                if (tabPosition == 0)
                    shareIMEI = (fragment as Tab1).copyAll()
                else if (tabPosition == 1)
                    shareIMEI = (fragment as Tab2).copyAll()
                if (shareIMEI != null) {
                    if (shareIMEI == "a") {
                        shakeIt()
                        showSnackBar(getString(R.string.data_too_big))
                        return true
                    }
                    if (itemId == R.id.action_share)
                        shareAll(shareIMEI)
                    else
                        copyToClipBoard(shareIMEI)
                    return true
                }
                dataEmptySnackBarMsg()
                return false
            } else if (itemId == R.id.action_save) {

                grantStoragePermission()
                return true

            } else if (itemId == R.id.action_clear) {
                shakeIt()
                if (tabPosition == 0)
                    (fragment as Tab1).clearAll()
                else if (tabPosition == 1)
                    (fragment as Tab2).clearAll()
                dataClearedSnackBarMsg()
                return true
            } else {
                return false
            }
        }
    }
    private val STORAGE_PERMISSION_KEY = "storage.permission.key"


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                // proceedAfterPermission();
                saveOption()
            } else {
                showSnackBar(getString(R.string.cross))
            }
        }
    }

    private fun saveOption(): Boolean {
        val fragment = mPagerAdapter!!.getFragment(mTabLayout.selectedTabPosition) ?: return false
        var b = false
        if (tabPosition == 0)
            b = (fragment as Tab1).save()
        else if (tabPosition == 1)
            b = (fragment as Tab2).save()
        if (!b) dataEmptySnackBarMsg()
        return b
    }

    private fun grantStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(WRITE_EXTERNAL_STORAGE),
                        EXTERNAL_STORAGE_PERMISSION_CONSTANT)
                // this is where you can explain why you need the permission more or less like the first time
                //Show Information about why you need the permission
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Need Storage Permission")
                builder.setMessage("This app needs storage permission.")
                builder.setPositiveButton("Grant") { dialog, which ->
                    dialog.cancel()
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(WRITE_EXTERNAL_STORAGE), EXTERNAL_STORAGE_PERMISSION_CONSTANT)
                }
                builder.setNegativeButton(getString(android.R.string.cancel)) { dialog, which -> dialog.cancel() }
                builder.show()

            } else if (storagePermissionStatus!!.getBoolean(STORAGE_PERMISSION_KEY)) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Need Storage Permission")
                builder.setMessage("This app needs storage permission.")
                builder.setPositiveButton("Grant") { dialog, which ->
                    dialog.cancel()
                    sentToSettings = true
                    launchSettings(STORAGE_PERMISSION_REQUEST_CODE, "Go to Permissions to Grant \"Storage\"")
                }
                builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
                builder.show()
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(WRITE_EXTERNAL_STORAGE), EXTERNAL_STORAGE_PERMISSION_CONSTANT)
            }

            storagePermissionStatus!!.putBoolean(STORAGE_PERMISSION_KEY, true)


        } else
        //You already have the permission, just go ahead.
            saveOption()
    }


    private fun launchSettings(REQUEST_CODE: Int, message: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, REQUEST_CODE)
        Toast.makeText(baseContext, message, Toast.LENGTH_LONG).show()
    }

    private fun dataEmptySnackBarMsg() {
        make(mConstraintLayout!!, getString(R.string.empty), LENGTH_SHORT).show()
        shakeIt()
    }

    private fun shareAll(textToShare: String?) {
        var i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, textToShare)
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        i = Intent.createChooser(i, getString(R.string.share_via))
        startActivity(i)
    }

    private fun copyToClipBoard(imei: String?) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", imei)
        clipboard.primaryClip = clip
        make(mConstraintLayout, getString(R.string.imei_copied), LENGTH_LONG).show()
        val params = Bundle()
        params.putString("imei_copied", imei)
        mFirebaseAnalytics.logEvent("copy_to_clipboard", params)
    }

    private fun shakeIt() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator ?: return
        if (Build.VERSION.SDK_INT >= 26)
            vibrator.vibrate(VibrationEffect.createOneShot(100, 10))
        else
            vibrator.vibrate(100)



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this, "ca-app-pub-9297690518647609~8655072260")
        val toolbar = findViewById<Toolbar>(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        storagePermissionStatus = TinyDB(this)
        mConstraintLayout =findViewById(R.id.constraintLayout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.itemIconTintList = null

        mFabSpeedDial = findViewById<FabSpeedDial>(R.id.fabSpeedDial)
        mTabLayout = findViewById(R.id.tab_layout)
        mViewPager = findViewById(R.id.viewPager)
        drawer = findViewById(R.id.drawer_layout)
        mAdView = findViewById(R.id.adView)
        navigationView.setNavigationItemSelectedListener(this)
        mFabSpeedDial.setMenuListener(fabMenuListener)
        mAdView!!.loadAd(AdRequest.Builder().build())
        mPagerAdapter = MyPagerAdapter(supportFragmentManager, mTabLayout.tabCount)
        mViewPager.adapter = mPagerAdapter
        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.main_activity_interstitial_adunit_id)
        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                requestNewInterstitial()
            }
        }
        requestNewInterstitial()

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = RewardedAdClass()
        loadRewardedVideoAd()


        val ratingDialog = RatingDialog.Builder(this)
                .threshold(4f)
                .session(6)
                .onRatingBarFormSumbit { feedback -> launchEmail("Rating FeedBack for IMEI Generator Lite",feedback) }.build()
        ratingDialog.show()
    }

    public override fun onResume() {
        super.onResume()
        mAdView.resume()
        mFabSpeedDial.setMenuListener(fabMenuListener)
        mTabLayout.addOnTabSelectedListener(mTabSelectedListener)
        mViewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    public override fun onPause() {
        super.onPause()
        mTabLayout.removeOnTabSelectedListener(mTabSelectedListener)
        mViewPager.removeOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(mTabLayout))
        drawer.removeDrawerListener(toggle)
        mAdView.pause()
    }

    
    private fun launchEmail(subject:String, mail_content: String){
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", getString(R.string.dev_email), null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, mail_content)
        try {
            startActivity(Intent.createChooser(emailIntent, "Send email..."))
        } catch (e: Exception) {
            // Implement Crashlytics
        }

    }

    private fun requestNewInterstitial() {
        mInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    internal fun showSnackBar(msg: String) {
        val snackbar = Snackbar.make(mConstraintLayout, msg, LENGTH_INDEFINITE)
        snackbar.setAction(android.R.string.ok) {
            displayAd()
            snackbar.dismiss()
        }
        snackbar.setActionTextColor(Color.YELLOW)
        snackbar.show()
    }

    private fun dataClearedSnackBarMsg() {
        make(mConstraintLayout, getString(R.string.cleared), LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.change_lang ->
                /*Intent intent = new Intent(this, LanguageActivity.class);
                startActivityForResult(intent, CHANGE_LANG_RC);*/
                return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun displayAd() {
        if (Singleton.getInstance().isTrue) {
            if (mInterstitialAd.isLoaded)
                mInterstitialAd.show()
            else {
                requestNewInterstitial()
                if (mRewardedVideoAd.isLoaded)
                    mRewardedVideoAd.show()
                else
                    loadRewardedVideoAd()
            }
        } else if (!mInterstitialAd.isLoaded || !mRewardedVideoAd.isLoaded) {
            if (!mInterstitialAd.isLoaded)
                requestNewInterstitial()
            if (!mRewardedVideoAd.isLoaded)
                loadRewardedVideoAd()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHANGE_LANG_RC) {
            if (data == null)
                return
            val shortcode = data.getStringExtra(LANG_SHORTCODE_KEY)
            val coutrycode = data.getStringExtra(LANG_COUNTRY_CODE_KEY)
            val locale = Locale(shortcode, coutrycode)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            baseContext.resources.updateConfiguration(config,
                    baseContext.resources.displayMetrics)
            val intent = intent
            finish()
            startActivity(intent)
        }
    }

    override fun clickedGenerate() {
        displayAd()
    }

    override fun onSaveFinished(message: String) {
        showSnackBar(message)
    }

    override fun vibrate() {
        shakeIt()
    }

    override fun vibrateCallback() {
        shakeIt()
    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("", AdRequest.Builder().build())
    }


    override fun onDestroy() {
        super.onDestroy()
        mAdView.destroy()
    }
    private fun launchWebSite(urlString: String): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString))
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            return true
        }
        return false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        var itemName = ""
        when (id) {
            R.id.rate_app -> rateApp()
            R.id.status_saver -> launchPlaystore("com.edgedevstudio.statussaver")
            R.id.app_extractor -> launchPlaystore("io.edgedev.appextractor")
            R.id.root_checker -> launchPlaystore("com.edgedevstudio.imei_toolbox")
            R.id.imei_checker -> launchPlaystore("com.edgedevstudio.imei_toolbox")
            R.id.imei_gen -> launchPlaystore("com.edgedevstudio.imei_toolbox")
            R.id.other_apps -> launchDevProfile()
            R.id.share_app -> shareApp()
            R.id.feedback -> {
                itemName = "Contact Us"
                val bool = launchWebSite("fb-messenger://user/1765492347080278")
                if (!bool)
                    launchEmail("Feedback for IMEI Generator Lite", "Hello")
            }
            R.id.telegram -> {
                itemName = "Join Telegram Group"
                launchWebSite("https://t.me/joinchat/F2C1Sgq7FAfAORxy6zPQwg")
            }
            R.id.fb_like -> {
                itemName = "Open Facebook Page"
                val bool = launchWebSite("fb://page/1765492347080278")
                if (!bool)
                    launchWebSite("https://web.facebook.com/imeigenerator")
            }
        }
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        logEvents("nav_item_selected", bundle)
        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    private fun logEvents(key: String, bundle: Bundle) {
        mFirebaseAnalytics.logEvent(key, bundle)
    }

    private fun launchPlaystore(path: String) {
        val playstoreUrl = "market://details?id=$path"
        val marketUri = Uri.parse(playstoreUrl)
        val intent = Intent(Intent.ACTION_VIEW, marketUri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(intent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, R.string.playstore_not_found, Toast.LENGTH_SHORT).show()
        }

    }

    private fun launchDevProfile() {
        val playstoreUrl = "market://search?q=pub:Edge+Dev+Studio"
        val marketUri = Uri.parse(playstoreUrl)
        val intent = Intent(Intent.ACTION_VIEW, marketUri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(intent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Couldn't find PlayStore on this device", Toast.LENGTH_SHORT).show()
        }

    }

    private fun shareApp() {
        val share_app = ShareCompat.IntentBuilder.from(this)
                .setSubject(getString(R.string.app_name))
                .setText(getString(R.string.app_name) + "- bit.ly/IMEI-Generator").intent
        share_app.type = "text/plain"
        startActivity(share_app)
    }

    private fun rateApp() {
        val ratingDialog = RatingDialog.Builder(this)
                .threshold(4f)
                .onRatingBarFormSumbit { feedback -> launchEmail("Rating FeedBack for IMEI Generator Lite", feedback) }.build()
        ratingDialog.show()
    }

    override fun clickedOneMonth() {

    }

    override fun clickedPermanent() {

    }

    override fun clickedRateApp() {
        rateApp()
    }

    override fun clickedCopyBtn(s: String) {
        copyToClipBoard(s)
        displayAd()
    }

    companion object {

        val CHANGE_LANG_RC = 1
        private val EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100
        private val STORAGE_PERMISSION_REQUEST_CODE = 101

        private val TAG = "MainActivity"
    }
}
