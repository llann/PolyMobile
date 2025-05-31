import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AppBarConfiguration appBarConfiguration;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String currentUser;
    private byte[] currentTag = new byte[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set up crash handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception: " + throwable.getMessage(), throwable);
            Toast.makeText(MainActivity.this, "App crashed: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        });

        super.onCreate(savedInstanceState);
        Log.d(TAG, "Starting onCreate");
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Setting up UI components");
        try {
            drawerLayout = findViewById(R.id.drawer_layout);
            NavigationView navigationView = findViewById(R.id.nav_view);
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            // Set up Navigation Component
            Log.d(TAG, "Setting up Navigation Component");
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_home, R.id.nav_menus, R.id.nav_gallery, R.id.nav_slideshow)
                    .setOpenableLayout(drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        } catch (Exception e) {
            Log.e(TAG, "Navigation setup failed: " + e.getMessage());
            Toast.makeText(this, "Navigation setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Set up NFC
        Log.d(TAG, "Setting up NFC");
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter == null) {
                Log.w(TAG, "NFC is not available");
                Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
            handleIntent(getIntent());
        } catch (Exception e) {
            Log.e(TAG, "NFC setup failed: " + e.getMessage());
            Toast.makeText(this, "NFC setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "onCreate completed");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Creating options menu");
        try {
            getMenuInflater().inflate(R.menu.main, menu);
            MenuItem searchItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchItem.getActionView();
            SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d(TAG, "Search query submitted: " + query);
                    if (query != null && !query.isEmpty()) {
                        currentUser = query;
                        currentTag = new byte[0];
                        try {
                            navController.navigate(R.id.nav_home);
                        } catch (Exception e) {
                            Log.e(TAG, "Search navigation failed: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
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
        } catch (Exception e) {
            Log.e(TAG, "Menu creation failed: " + e.getMessage());
            Toast.makeText(this, "Menu setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navigating up");
        try {
            return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
        } catch (Exception e) {
            Log.e(TAG, "Navigation up failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed");
        try {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
            Log.e(TAG, "Back press failed: " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resuming, enabling NFC");
        try {
            if (nfcAdapter != null) {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "NFC resume failed: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing, disabling NFC");
        try {
            if (nfcAdapter != null) {
                nfcAdapter.disableForegroundDispatch(this);
            }
        } catch (Exception e) {
            Log.e(TAG, "NFC pause failed: " + e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "Received new intent: " + intent.getAction());
        try {
            setIntent(intent);
            handleIntent(intent);
        } catch (Exception e) {
            Log.e(TAG, "New intent handling failed: " + e.getMessage());
        }
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG, "Handling intent: " + intent.getAction());
        try {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                if (tag != null) {
                    currentTag = tag.getId();
                    currentUser = null;
                    navController.navigate(R.id.nav_home);
                    Toast.makeText(this, "NFC Tag: " + bytesToHex(currentTag), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "NFC handling failed: " + e.getMessage());
            Toast.makeText(this, "NFC handling failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}