package com.movtery.pojavzh.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.movtery.anim.AnimPlayer
import com.movtery.anim.animations.Animations
import com.movtery.pojavzh.feature.accounts.LocalAccountUtils.CheckResultListener
import com.movtery.pojavzh.feature.accounts.LocalAccountUtils.Companion.checkUsageAllowed
import com.movtery.pojavzh.feature.accounts.LocalAccountUtils.Companion.openDialog
import com.movtery.pojavzh.setting.AllSettings.Companion.localAccountReminders
import com.movtery.pojavzh.ui.dialog.TipDialog
import com.movtery.pojavzh.utils.ZHTools
import net.kdt.pojavlaunch.R
import net.kdt.pojavlaunch.databinding.FragmentSelectAuthMethodBinding
import net.kdt.pojavlaunch.fragments.LocalLoginFragment
import net.kdt.pojavlaunch.fragments.MicrosoftLoginFragment

class SelectAuthFragment : FragmentWithAnim(R.layout.fragment_select_auth_method) {
    companion object {
        const val TAG: String = "AUTH_SELECT_FRAGMENT"
    }

    private lateinit var binding: FragmentSelectAuthMethodBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectAuthMethodBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fragmentActivity = requireActivity()
        binding.returnButton.setOnClickListener {
            ZHTools.onBackPressed(fragmentActivity)
        }
        binding.buttonMicrosoftAuthentication.setOnClickListener {
            swapToFragment(MicrosoftLoginFragment::class.java, MicrosoftLoginFragment.TAG)
        }
        binding.buttonOtherAuthentication.setOnClickListener {
            checkUsageAllowed(object : CheckResultListener {
                override fun onUsageAllowed() {
                    swapToOther()
                }

                override fun onUsageDenied() {
                    if (!localAccountReminders) {
                        swapToOther()
                    } else {
                        openDialog(
                            fragmentActivity,
                            TipDialog.OnConfirmClickListener { swapToOther() },
                            getString(R.string.account_no_microsoft_account_other) + getString(R.string.account_purchase_minecraft_account_tip),
                            R.string.account_no_microsoft_account_other_confirm
                        )
                    }
                }
            })
        }

        binding.buttonLocalAuthentication.setOnClickListener {
            checkUsageAllowed(object : CheckResultListener {
                override fun onUsageAllowed() {
                    swapToLocal()
                }

                override fun onUsageDenied() {
                    if (!localAccountReminders) {
                        swapToLocal()
                    } else {
                        openDialog(
                            fragmentActivity,
                            TipDialog.OnConfirmClickListener { swapToLocal() },
                            getString(R.string.account_no_microsoft_account_local) + getString(R.string.account_purchase_minecraft_account_tip),
                            R.string.account_no_microsoft_account_local_confirm
                        )
                    }
                }
            })
        }
    }

    private fun swapToOther() {
        swapToFragment(OtherLoginFragment::class.java, OtherLoginFragment.TAG)
    }

    private fun swapToLocal() {
        swapToFragment(LocalLoginFragment::class.java, LocalLoginFragment.TAG)
    }

    private fun swapToFragment(clazz: Class<out Fragment>, tag: String, bundle: Bundle? = null) {
        ZHTools.swapFragmentWithAnim(this, clazz, tag, bundle)
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.getRoot(), Animations.BounceInDown))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.getRoot(), Animations.FadeOutUp))
    }
}
