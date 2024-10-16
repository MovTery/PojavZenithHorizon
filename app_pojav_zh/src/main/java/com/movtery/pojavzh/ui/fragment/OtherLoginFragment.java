package com.movtery.pojavzh.ui.fragment;

import static net.kdt.pojavlaunch.Tools.runOnUiThread;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.movtery.anim.AnimPlayer;
import com.movtery.anim.animations.Animations;
import com.movtery.pojavzh.event.value.OtherLoginEvent;
import com.movtery.pojavzh.feature.log.Logging;
import com.movtery.pojavzh.feature.login.AuthResult;
import com.movtery.pojavzh.feature.login.OtherLoginApi;
import com.movtery.pojavzh.feature.login.Servers;
import com.movtery.pojavzh.ui.dialog.EditTextDialog;
import com.movtery.pojavzh.ui.dialog.ProgressDialog;
import com.movtery.pojavzh.ui.dialog.SelectRoleDialog;
import com.movtery.pojavzh.ui.dialog.TipDialog;
import com.movtery.pojavzh.utils.PathAndUrlManager;
import com.movtery.pojavzh.utils.ZHTools;
import com.movtery.pojavzh.utils.stringutils.StringUtils;
import com.skydoves.powerspinner.DefaultSpinnerAdapter;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.databinding.FragmentOtherLoginBinding;
import net.kdt.pojavlaunch.value.MinecraftAccount;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OtherLoginFragment extends FragmentWithAnim {
    public static final String TAG = "OtherLoginFragment";
    public String mCurrentBaseUrl;
    private FragmentOtherLoginBinding binding;
    private ProgressDialog mProgressDialog;
    private File mServersFile;
    private Servers mServers;
    private List<String> mServerList;
    private String mCurrentRegisterUrl;
    private DefaultSpinnerAdapter mServerSpinnerAdapter;

    public OtherLoginFragment() {
        super(R.layout.fragment_other_login);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtherLoginBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mServersFile = new File(PathAndUrlManager.DIR_GAME_HOME, "servers.json");
        mProgressDialog = new ProgressDialog(requireContext(), () -> true);
        mProgressDialog.updateText(getString(R.string.account_login_start));

        refreshServer();
        showRegisterButton(); //刷新注册按钮

        binding.returnButton.setOnClickListener(v -> ZHTools.onBackPressed(requireActivity()));
        binding.serverSpinner.setSpinnerAdapter(mServerSpinnerAdapter);
        binding.serverSpinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (i, s, i1, t1) -> {
            if (!Objects.isNull(mServers)) {
                for (Servers.Server server : mServers.getServer()) {
                    if (server.getServerName().equals(mServerList.get(i1))) {
                        mCurrentBaseUrl = server.getBaseUrl();
                        mCurrentRegisterUrl = server.getRegister();
                        Logging.e("Other Login", "currentRegisterUrl:" + mCurrentRegisterUrl);
                    }
                }
            }
        });
        binding.serverSpinner.selectItemByIndex(0);

        binding.addServer.setOnClickListener(v -> new TipDialog.Builder(requireContext())
                .setMessage(getString(R.string.other_login_add_server))
                .setCancel(R.string.other_login)
                .setConfirm(R.string.other_login_uniform_pass)
                .setCancelClickListener(() -> showServerTypeSelectDialog(R.string.other_login_yggdrasil_api, 0))
                .setConfirmClickListener(() -> showServerTypeSelectDialog(R.string.other_login_32_bit_server, 1))
                .buildDialog());

        binding.register.setOnClickListener(v -> {
            if (!Objects.isNull(mCurrentRegisterUrl)) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri url = Uri.parse(mCurrentRegisterUrl);
                intent.setData(url);
                startActivity(intent);
            }
        });

        binding.otherLoginButton.setOnClickListener(v -> PojavApplication.sExecutorService.execute(() -> {
            String user = binding.loginEditEmail.getText().toString();
            String pass = binding.loginEditPassword.getText().toString();
            String baseUrl = mCurrentBaseUrl;

            if (!checkAccountInformation(user, pass)) return;

            if (!(baseUrl == null || baseUrl.isEmpty())) {
                requireActivity().runOnUiThread(() -> mProgressDialog.show());
                try {
                    OtherLoginApi.INSTANCE.setBaseUrl(mCurrentBaseUrl);
                    OtherLoginApi.INSTANCE.login(requireContext(), user, pass, new OtherLoginApi.Listener() {
                        @Override
                        public void onSuccess(@NonNull AuthResult authResult) {
                            requireActivity().runOnUiThread(() -> {
                                mProgressDialog.dismiss();
                                MinecraftAccount account = new MinecraftAccount();
                                account.accessToken = authResult.getAccessToken();
                                account.clientToken = authResult.getClientToken();
                                account.baseUrl = mCurrentBaseUrl;
                                account.account = binding.loginEditEmail.getText().toString();
                                account.expiresAt = ZHTools.getCurrentTimeMillis() + 30 * 60 * 1000;
                                if (!Objects.isNull(authResult.getSelectedProfile())) {
                                    account.username = authResult.getSelectedProfile().getName();
                                    account.profileId = authResult.getSelectedProfile().getId();
                                    EventBus.getDefault().post(new OtherLoginEvent(account));
                                    Tools.backToMainMenu(requireActivity());
                                } else {
                                    SelectRoleDialog selectRoleDialog = new SelectRoleDialog(requireContext(), authResult.getAvailableProfiles());
                                    selectRoleDialog.setOnSelectedListener(selectedProfile -> {
                                        account.profileId = selectedProfile.getId();
                                        account.username = selectedProfile.getName();
                                        refresh(account);
                                    });
                                    selectRoleDialog.show();
                                }
                            });
                        }

                        @Override
                        public void onFailed(@NonNull String error) {
                            requireActivity().runOnUiThread(() -> {
                                mProgressDialog.dismiss();
                                new TipDialog.Builder(requireContext())
                                        .setTitle(R.string.generic_warning)
                                        .setMessage(getString(R.string.other_login_error) + error)
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
                runOnUiThread(() -> Toast.makeText(requireContext(), getString(R.string.other_login_server_not_empty), Toast.LENGTH_SHORT).show());
            }
        }));
    }

    @Override
    public void onPause() {
        binding.serverSpinner.dismiss();
        super.onPause();
    }

    private void showServerTypeSelectDialog(int stringId, int type) {
        new EditTextDialog.Builder(requireContext())
                .setTitle(stringId)
                .setConfirmListener(editBox -> {
                    if (editBox.getText().toString().isEmpty()) {
                        editBox.setError(getString(R.string.generic_error_field_empty));
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
                runOnUiThread(() -> binding.loginEditEmail.setError(getString(R.string.generic_error_field_empty)));
            }
            if (passTextEmpty) {
                runOnUiThread(() -> binding.loginEditPassword.setError(getString(R.string.generic_error_field_empty)));
            }
            return false;
        } else {
            return true;
        }
    }

    private void addServer(EditText editText, int type) {
        requireActivity().runOnUiThread(() -> mProgressDialog.show());
        PojavApplication.sExecutorService.execute(() -> {
            String data = OtherLoginApi.INSTANCE.getServeInfo(type == 0 ? editText.getText().toString() : "https://auth.mc-user.com:233/" + editText.getText().toString());
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

    private void refresh(MinecraftAccount account) {
        PojavApplication.sExecutorService.execute(() -> {
            try {
                OtherLoginApi.INSTANCE.refresh(requireContext(), account, true, new OtherLoginApi.Listener() {
                    @Override
                    public void onSuccess(@NonNull AuthResult authResult) {
                        account.accessToken = authResult.getAccessToken();
                        EventBus.getDefault().post(new OtherLoginEvent(account));
                        requireActivity().runOnUiThread(() -> Tools.backToMainMenu(requireActivity()));
                    }

                    @Override
                    public void onFailed(@NonNull String error) {
                        requireActivity().runOnUiThread(() -> new TipDialog.Builder(requireContext())
                                .setTitle(R.string.generic_warning)
                                .setMessage(getString(R.string.other_login_error) + error)
                                .setCenterMessage(false)
                                .setCancel(android.R.string.copy)
                                .setCancelClickListener(() -> StringUtils.copyText("error", error, requireContext()))
                                .buildDialog());
                    }
                });
            } catch (IOException e) {
                Logging.e("other login", Tools.printToString(e));
            }
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
            mServerList.add(getString(R.string.other_login_no_server));
        }
        if (Objects.isNull(mServerSpinnerAdapter)) {
            mServerSpinnerAdapter = new DefaultSpinnerAdapter(binding.serverSpinner);
        }
        mServerSpinnerAdapter.setItems(mServerList);
    }

    private void showRegisterButton() {
        //当服务器列表为空、服务器列表没有可用服务器时，注册按钮将被隐藏
        binding.register.setVisibility((mServerList == null ||
                (mServerList.size() == 1 && mServerList.get(0).equals(getString(R.string.other_login_no_server))))
                ? View.GONE : View.VISIBLE);
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.getRoot(), Animations.BounceInDown));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.getRoot(), Animations.FadeOutUp));
    }
}