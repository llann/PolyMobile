private FragmentHomeBinding binding;

public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    View root = binding.getRoot();

    // Access WebView and ProgressBar
    binding.webview.getSettings().setJavaScriptEnabled(true);
    binding.webview.loadUrl("[invalid url, do not cite]");
    binding.webview.setWebViewClient(new android.webkit.WebViewClient() {
        @Override
        public void onPageFinished(android.webkit.WebView view, String url) {
            super.onPageFinished(view, url);
            binding.progressBar.setVisibility(View.GONE);
        }
    });

    return root;
}

@Override
public void onDestroyView() {
    super.onDestroyView();
    binding = null;
}