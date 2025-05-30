package ch.polylan.polymobile;

import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressBar progressBar;
    private WebView webView;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String currentUrl;
    private byte[] currentTag = new byte[0];
    private String currentUser;
    private static final String BASE_URL = "[invalid url, do not cite]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webview);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        currentUrl = getString(R.string.url_home);

        // Set up navigation drawer
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(this);

        // Set up WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String session = getCookie(getString(R.string.cookie_session));
                if (session != null) {
                    CookieManager.getInstance().setCookie(BASE_URL + currentUrl, "sessionid=" + session);
                }
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        reload();

        // Set up NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        handleIntent(getIntent());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            currentUrl = getString(R.string.url_home);
        } else if (id == R.id.nav_menus) {
            currentUrl = getString(R.string.url_menu);
        }
        reload();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null && !query.isEmpty()) {
                    currentUser = query;
                    currentTag = new byte[0];
                    reload();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.search_warning), Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                currentTag = tag.getId();
                currentUser = null;
                reload();
            }
        }
    }

    private void reload() {
        String url = BASE_URL + currentUrl;
        if (currentTag.length > 0) {
            url += "?card_id=" + bytesToHex(currentTag);
        } else if (currentUser != null) {
            url += "?user_id=" + currentUser;
        }
        webView.loadUrl(url);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }

    private String getCookie(String name) {
        String cookies = CookieManager.getInstance().getCookie(BASE_URL + currentUrl);
        if (cookies != null) {
            for (String cookie : cookies.split(";")) {
                String trimmedCookie = cookie.trim();
                if (trimmedCookie.startsWith(name + "=")) {
                    return trimmedCookie.substring(name.length() + 1);
                }
            }
        }
        return null;
    }
}