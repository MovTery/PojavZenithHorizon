package net.kdt.pojavlaunch.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.movtery.pojavzh.event.value.MicrosoftLoginEvent;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.utils.ZHTools;

import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.FragmentMicrosoftLoginBinding;

import org.greenrobot.eventbus.EventBus;

public class MicrosoftLoginFragment extends Fragment {
    public static final String TAG = "MICROSOFT_LOGIN_FRAGMENT";
    private FragmentMicrosoftLoginBinding binding;
    // Technically the client is blank (or there is none) when the fragment is initialized
    private boolean mBlankClient = true;

    public MicrosoftLoginFragment() {
        super(R.layout.fragment_microsoft_login);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMicrosoftLoginBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.returnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));

        setWebViewSettings();
        if(savedInstanceState == null) startNewSession();
        else restoreWebViewState(savedInstanceState);
    }

    // WebView.restoreState() does not restore the WebSettings or the client, so set them there
    // separately. Note that general state should not be altered here (aka no loading pages, no manipulating back/front lists),
    // to avoid "undesirable side-effects"
    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewSettings() {
        WebSettings settings = binding.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        binding.webView.setWebViewClient(new WebViewTrackClient());
        mBlankClient = false;
    }

    private void startNewSession() {
        CookieManager.getInstance().removeAllCookies((b)->{
            binding.webView.clearHistory();
            binding.webView.clearCache(true);
            binding.webView.clearFormData();
            binding.webView.clearHistory();
            binding.webView.loadUrl("https://login.live.com/oauth20_authorize.srf" +
                    "?client_id=00000000402b5328" +
                    "&response_type=code" +
                    "&scope=service%3A%3Auser.auth.xboxlive.com%3A%3AMBI_SSL" +
                    "&redirect_url=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf");
        });
    }

    private void restoreWebViewState(Bundle savedInstanceState) {
        Logging.i("MSAuthFragment","Restoring state...");
        if(binding.webView.restoreState(savedInstanceState) == null) {
            Logging.w("MSAuthFragment", "Failed to restore state, starting afresh");
            // if, for some reason, we failed to restore our session,
            // just start afresh
            startNewSession();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If we have switched to a blank client and haven't fully gone though the lifecycle callbacks to restore it,
        // restore it here.
        if(mBlankClient) binding.webView.setWebViewClient(new WebViewTrackClient());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // Since the value cannot be null, just create a "blank" client. This is done to not let Android
        // kill us if something happens after the state gets saved, when we can't do fragment transitions
        binding.webView.setWebViewClient(new WebViewClient());
        // For some dumb reason state is saved even when Android won't actually destroy the activity.
        // Let the fragment know that the client is blank so that we can restore it in onStart()
        // (it was the earliest lifecycle call actually invoked in this case)
        mBlankClient = true;
        super.onSaveInstanceState(outState);
        binding.webView.saveState(outState);
    }

    /* Expose webview actions to others */
    public boolean canGoBack(){ return binding.webView.canGoBack();}
    public void goBack(){ binding.webView.goBack();}

    /** Client to track when to sent the data to the launcher */
    class WebViewTrackClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.startsWith("ms-xal-00000000402b5328")) {
                // Should be captured by the activity to kill the fragment and get
                EventBus.getDefault().post(new MicrosoftLoginEvent(Uri.parse(url)));
                Toast.makeText(view.getContext(), getString(R.string.zh_account_login_start), Toast.LENGTH_SHORT).show();
                Tools.backToMainMenu(requireActivity());

                return true;
            }

            // Sometimes, the user just clicked cancel
            if(url.contains("res=cancel")){
                Tools.backToMainMenu(requireActivity());
                return true;
            }


            return super.shouldOverrideUrlLoading(view, url);
        }
    }


}
