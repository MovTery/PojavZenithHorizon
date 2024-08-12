package com.movtery.pojavzh.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.movtery.pojavzh.extra.ZHExtraConstants;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.feature.login.AuthResult;
import com.movtery.pojavzh.feature.login.OtherLoginApi;
import com.movtery.pojavzh.feature.login.Servers;
import com.movtery.pojavzh.ui.dialog.EditTextDialog;
import com.movtery.pojavzh.ui.dialog.ProgressDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.anim.ViewAnimUtils;
import com.movtery.pojavzh.utils.stringutils.StringUtils;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OtherLoginFragment extends FragmentWithAnim {
    public static final String TAG = "OtherLoginFragment";
    public String mCurrentBaseUrl;
    private View mMainView;
    private ProgressDialog mProgressDialog;
    private Spinner mServerSpinner;
    private EditText mUserEditText, mPassEditText;
    private Button mLoginButton;
    private TextView mRegister;
    private ImageButton mAddServer;
    private ImageView mReturnButton;
    private File mServersFile;
    private Servers mServers;
    private List<String> mServerList;
    private String mCurrentRegisterUrl;
    private ArrayAdapter<String> mServerSpinnerAdapter;

    public OtherLoginFragment() {
        super(R.layout.fragment_other_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        mServersFile = new File(PathAndUrlManager.DIR_GAME_HOME, "servers.json");
        mProgressDialog = new ProgressDialog(requireContext(), () -> true);
        mProgressDialog.updateText(getString(R.string.zh_account_login_start));

        refreshServer();
        showRegisterButton(); //刷新注册按钮

        mReturnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
        mServerSpinner.setAdapter(mServerSpinnerAdapter);
        mServerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!Objects.isNull(mServers)) {
                    for (Servers.Server server : mServers.getServer()) {
                        if (server.getServerName().equals(mServerList.get(i))) {
                            mCurrentBaseUrl = server.getBaseUrl();
                            mCurrentRegisterUrl = server.getRegister();
                            Logging.e("test", "currentRegisterUrl:" + mCurrentRegisterUrl);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mAddServer.setOnClickListener(v -> new TipDialog.Builder(requireContext())
                .setMessage(getString(R.string.zh_other_login_add_server))
                .setCancel(R.string.zh_other_login_external_login)
                .setConfirm(R.string.zh_other_login_uniform_pass)
                .setCancelClickListener(() -> showServerTypeSelectDialog(R.string.zh_other_login_yggdrasil_api, 0))
                .setConfirmClickListener(() -> showServerTypeSelectDialog(R.string.zh_other_login_32_bit_server, 1))
                .buildDialog());

        mRegister.setOnClickListener(v -> {
            if (!Objects.isNull(mCurrentRegisterUrl)) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri url = Uri.parse(mCurrentRegisterUrl);
                intent.setData(url);
                startActivity(intent);
            }
        });

        mLoginButton.setOnClickListener(v -> PojavApplication.sExecutorService.execute(() -> {
            String user = mUserEditText.getText().toString();
            String pass = mPassEditText.getText().toString();
            String baseUrl = mCurrentBaseUrl;

            if (!checkAccountInformation(user, pass)) return;

            if (!(baseUrl == null || baseUrl.isEmpty())) {
                requireActivity().runOnUiThread(() -> mProgressDialog.show());
                try {
                    OtherLoginApi.getINSTANCE().setBaseUrl(mCurrentBaseUrl);
                    OtherLoginApi.getINSTANCE().login(getContext(), user, pass, new OtherLoginApi.Listener() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            requireActivity().runOnUiThread(() -> {
                                mProgressDialog.dismiss();
                                MinecraftAccount account = new MinecraftAccount();
                                account.accessToken = authResult.getAccessToken();
                                account.baseUrl = mCurrentBaseUrl;
                                account.account = mUserEditText.getText().toString();
                                account.password = mPassEditText.getText().toString();
                                account.expiresAt = ZHTools.getCurrentTimeMillis() + 30 * 60 * 1000;
                                if (!Objects.isNull(authResult.getSelectedProfile())) {
                                    account.username = authResult.getSelectedProfile().getName();
                                    account.profileId = authResult.getSelectedProfile().getId();
                                    ExtraCore.setValue(ZHExtraConstants.OTHER_LOGIN_TODO, account);
                                    Tools.backToMainMenu(requireActivity());
                                } else {
                                    List<String> list = new ArrayList<>();
                                    for (AuthResult.AvailableProfiles profiles : authResult.getAvailableProfiles()) {
                                        list.add(profiles.getName());
                                    }
                                    String[] items = list.toArray(new String[0]);
                                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                                            .setItems(items, (d, i) -> {
                                                for (AuthResult.AvailableProfiles profiles : authResult.getAvailableProfiles()) {
                                                    if (profiles.getName().equals(items[i])) {
                                                        account.profileId = profiles.getId();
                                                        account.username = profiles.getName();
                                                    }
                                                }
                                                ExtraCore.setValue(ZHExtraConstants.OTHER_LOGIN_TODO, account);
                                                Tools.backToMainMenu(requireActivity());
                                            })
                                            .setNegativeButton(getString(android.R.string.cancel), null)
                                            .create();
                                    dialog.show();
                                }
                            });
                        }

                        @Override
                        public void onFailed(String error) {
                            requireActivity().runOnUiThread(() -> {
                                mProgressDialog.dismiss();
                                new TipDialog.Builder(requireContext())
                                        .setTitle(R.string.zh_warning)
                                        .setMessage(getString(R.string.zh_other_login_error) + error)
                                        .setCancel(android.R.string.copy)
                                        .setCancelClickListener(() -> StringUtils.copyText("error", error, requireContext()))
                                        .buildDialog();
                            });
                        }
                    });
                } catch (IOException e) {
                    requireActivity().runOnUiThread(() -> mProgressDialog.dismiss());
                    Logging.e("login", e.toString());
                }
            } else {
                runOnUiThread(() -> Toast.makeText(requireContext(), getString(R.string.zh_other_login_server_not_empty), Toast.LENGTH_SHORT).show());
            }
        }));

        ViewAnimUtils.slideInAnim(this);
    }

    private void bindViews(@NonNull View view) {
        mMainView = view;
        mServerSpinner = view.findViewById(R.id.server_spinner);
        mUserEditText = view.findViewById(R.id.login_edit_email);
        mPassEditText = view.findViewById(R.id.login_edit_password);
        mLoginButton = view.findViewById(R.id.other_login_button);
        mRegister = view.findViewById(R.id.register);
        mAddServer = view.findViewById(R.id.add_server);
        mReturnButton = view.findViewById(R.id.zh_login_return);
    }

    private void showServerTypeSelectDialog(int stringId, int type) {
        new EditTextDialog.Builder(requireContext())
                .setTitle(stringId)
                .setConfirmListener(editBox -> {
                    if (editBox.getText().toString().isEmpty()) {
                        editBox.setError(getString(R.string.global_error_field_empty));
                        return false;
                    }

                    addServer(editBox, type);
                    return true;
                }).buildDialog();
    }

    private boolean checkAccountInformation(String user, String pass) {
        boolean userTextEmpty = user.isEmpty();
        boolean passTextEmpty = pass.isEmpty();

        if (userTextEmpty || passTextEmpty) {
            if (userTextEmpty) {
                runOnUiThread(() -> mUserEditText.setError(getString(R.string.global_error_field_empty)));
            }
            if (passTextEmpty) {
                runOnUiThread(() -> mPassEditText.setError(getString(R.string.global_error_field_empty)));
            }
            return false;
        } else {
            return true;
        }
    }

    private void addServer(EditText editText, int type) {
        requireActivity().runOnUiThread(() -> mProgressDialog.show());
        PojavApplication.sExecutorService.execute(() -> {
            String data = OtherLoginApi.getINSTANCE().getServeInfo(type == 0 ? editText.getText().toString() : "https://auth.mc-user.com:233/" + editText.getText().toString());
            requireActivity().runOnUiThread(() -> {
                mProgressDialog.dismiss();
                if (!Objects.isNull(data)) {
                    try {
                        Servers.Server server = new Servers.Server();
                        JSONObject jsonObject = new JSONObject(data);
                        JSONObject meta = jsonObject.optJSONObject("meta");
                        server.setServerName(meta.optString("serverName"));
                        server.setBaseUrl(editText.getText().toString());
                        if (type == 0) {
                            JSONObject links = meta.optJSONObject("links");
                            server.setRegister(links.optString("register"));
                        } else {
                            server.setBaseUrl("https://auth.mc-user.com:233/" + editText.getText().toString());
                            server.setRegister("https://login.mc-user.com:233/" + editText.getText().toString() + "/loginreg");
                        }
                        if (Objects.isNull(mServers)) {
                            mServers = new Servers();
                            mServers.setServer(new ArrayList<>());
                        }
                        mServers.getServer().add(server);
                        Tools.write(mServersFile.getAbsolutePath(), Tools.GLOBAL_GSON.toJson(mServers, Servers.class));
                        refreshServer();
                        mCurrentBaseUrl = server.getBaseUrl();
                        mCurrentRegisterUrl = server.getRegister();

                        showRegisterButton();
                    } catch (Exception e) {
                        Logging.e("add server", e.toString());
                    }
                }
            });
        });
    }

    private void refreshServer() {
        if (Objects.isNull(mServerList)) {
            mServerList = new ArrayList<>();
        } else {
            mServerList.clear();
        }
        if (mServersFile.exists()) {
            try {
                mServers = new Gson().fromJson(Tools.read(mServersFile.getAbsolutePath()), Servers.class);
                mCurrentBaseUrl = mServers.getServer().get(0).getBaseUrl();
                for (Servers.Server server : mServers.getServer()) {
                    mServerList.add(server.getServerName());
                }
            } catch (IOException ignored) {

            }
        }
        if (Objects.isNull(mServers)) {
            mServerList.add(getString(R.string.zh_other_login_no_server));
        }
        if (Objects.isNull(mServerSpinnerAdapter)) {
            mServerSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, mServerList);
        } else {
            mServerSpinnerAdapter.notifyDataSetChanged();
        }
    }

    private void showRegisterButton() {
        //当服务器列表为空、服务器列表没有可用服务器时，注册按钮将被隐藏
        mRegister.setVisibility((mServerList == null ||
                (mServerList.size() == 1 && mServerList.get(0).equals(getString(R.string.zh_other_login_no_server))))
                ? View.GONE : View.VISIBLE);
    }

    @Override
    public YoYo.YoYoString[] slideIn() {
        YoYo.YoYoString yoYoString = ViewAnimUtils.setViewAnim(mMainView, Techniques.BounceInDown);
        YoYo.YoYoString[] array = {yoYoString};
        super.setYoYos(array);
        return array;
    }

    @Override
    public YoYo.YoYoString[] slideOut() {
        YoYo.YoYoString yoYoString = ViewAnimUtils.setViewAnim(mMainView, Techniques.FadeOutUp);
        YoYo.YoYoString[] array = {yoYoString};
        super.setYoYos(array);
        return array;
    }
}