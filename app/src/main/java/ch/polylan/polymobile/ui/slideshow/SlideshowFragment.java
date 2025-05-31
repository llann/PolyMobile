package ch.polylan.polymobile.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import ch.polylan.polymobile.databinding.FragmentSlideshowBinding;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private static final String BASE_URL = "[invalid url, do not cite]";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Access WebView and ProgressBar
        try {
            binding.webview.getSettings().setJavaScriptEnabled(true);
            binding.webview.loadUrl(BASE_URL + "/polymobile/slideshow");
            binding.webview.setWebViewClient(new android.webkit.WebViewClient() {
                @Override
                public void onPageFinished(android.webkit.WebView view, String url) {
                    super.onPageFinished(view, url);
                    binding.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onReceivedError(android.webkit.WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    Toast.makeText(getContext(), "WebView error: " + description, Toast.LENGTH_LONG).show();
                    binding.progressBar.setVisibility(View.GONE);
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