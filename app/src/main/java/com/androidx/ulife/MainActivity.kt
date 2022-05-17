package com.androidx.ulife

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.androidx.ulife.dao.AppDatabase
import com.androidx.ulife.dao.HomePagePart
import com.androidx.ulife.databinding.ActivityMainBinding
import com.androidx.ulife.model.HomePagePartForm
import com.androidx.ulife.model.RefreshMode
import com.androidx.ulife.model.adPicItem
import com.blankj.utilcode.util.LogUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val homePageDao = AppDatabase.appDb.homePageDao()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAnchorView(R.id.fab)
                .setAction("Action", null).show()

            GlobalScope.launch(Dispatchers.IO) {
                val localList = arrayListOf(
                    HomePagePart(null, Random.nextInt(5), Random.nextInt(20), System.currentTimeMillis(), RefreshMode.ON_RESUME.ordinal, HomePagePartForm.GLOBAL.ordinal, randomData()),
                    HomePagePart(null, Random.nextInt(5), Random.nextInt(20), System.currentTimeMillis(), RefreshMode.ON_CREATE.ordinal, HomePagePartForm.GLOBAL.ordinal, randomData()),
                    HomePagePart(null, Random.nextInt(5), Random.nextInt(20), System.currentTimeMillis(), RefreshMode.ON_NEXT.ordinal, HomePagePartForm.GLOBAL.ordinal, randomData()),
                    HomePagePart(null, Random.nextInt(5), Random.nextInt(20), System.currentTimeMillis(), RefreshMode.ON_RESUME.ordinal, HomePagePartForm.GLOBAL.ordinal, randomData())
                )
                homePageDao.insert(localList)

                LogUtils.d("Part Info", homePageDao.queryPart(Random.nextInt(5)))

//                val partReq = homePageDao.queryPartReq(3)
//                delay(5000)
//                partReq?.let {
//                    it.updateTime = System.currentTimeMillis()
//                    it.version += 10
//                    homePageDao.updateReqPart(it)
//                }
            }
        }

//        GlobalScope.launch(Dispatchers.Main) {
//            val channel = ManagedChannelBuilder
//                .forAddress("192.168.31.198", 50051)
//                .usePlaintext()
//                .executor(Dispatchers.IO.asExecutor())
//                .build()
//
//            val hello = GreeterGrpcKt.GreeterCoroutineStub(channel)
//
//            val resp = withContext(Dispatchers.IO) {
//                hello.sayHello(helloRequest { name = "123" })
//            }
//            ToastUtils.showShort(resp.message)
//        }

//        val ulife = UlifeServiceGrpcKt.UlifeServiceCoroutineStub(channel)
//        val resp =  ulife.queryUlife(queryRequest {  })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun randomData(): ByteArray {
        return adPicItem {
            picUrl = "啊7777${Random.nextInt()}"
            clickAction = "我8888${Random.nextInt()}"
        }.toByteArray()
    }
}