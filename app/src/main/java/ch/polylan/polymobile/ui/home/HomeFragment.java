package ch.polylan.polymobile.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import ch.polylan.polymobile.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private static final String BASE_URL = "https://polylan.ch";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        try {
            binding.webview.getSettings().setJavaScriptEnabled(true);
            binding.webview.getSettings().setDomStorageEnabled(true);
            binding.webview.getSettings().setCacheMode(android.webkit.WebSettings.LOAD_DEFAULT);
            binding.webview.loadUrl(BASE_URL + "/polymobile/");
            binding.webview.setWebViewClient(new android.webkit.WebViewClient() {
                @Override
                public void onPageFinished(android.webkit.WebView view, String url) {
                    super.onPageFinished(view, url);
                    binding.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    Toast.makeText(getContext(), "WebView error: " + description + ". Check internet.", Toast.LENGTH_LONG).show();
                    binding.progressBar.setVisibility(View.GONE);
                    view.loadData("<html><body><h2>No Internet</h2><p>Please check your connection.</p></body></html>", "text/html", "UTF-8");
                }
            });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to load WebView: " + e.getMessage(), Toast.LENGTH_LONG).show();
            binding.progressBar.setVisibility(View.GONE);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}