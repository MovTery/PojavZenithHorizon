package net.kdt.pojavlaunch.modloaders.modpacks.api;

import android.content.Context;

import com.kdt.mcgui.ProgressLayout;
import com.movtery.pojavzh.feature.mod.ModFilters;
import com.movtery.pojavzh.ui.subassembly.downloadmod.ModVersionItem;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.R;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModDetail;
import net.kdt.pojavlaunch.modloaders.modpacks.models.ModItem;
import net.kdt.pojavlaunch.modloaders.modpacks.models.SearchResult;

import java.io.IOException;

/**
 *
 */
public interface ModpackApi {
    /**
     * Get the current mod's website link.
     * @param item Used to obtain ID information.
     * @return Website link, return null if retrieval fails.
     */
    String getWebUrl(ModItem item);

    /**
     * @param modFilters Filters
     * @param previousPageResult The result from the previous page
     * @return the list of mod items from specified offset
     */
    SearchResult searchMod(ModFilters modFilters, SearchResult previousPageResult);

    /**
     * @param modFilters Filters
     * @return A list of mod items
     */
    default SearchResult searchMod(ModFilters modFilters) {
        return searchMod(modFilters, null);
    }

    /**
     * Fetch the mod details
     * @param item The moditem that was selected
     * @return Detailed data about a mod(pack)
     */
    ModDetail getModDetails(ModItem item, boolean force);

    /**
     * Download and install the mod(pack)
     * @param modDetail The mod detail data
     * @param modVersionItem The selected version
     */
    default void handleInstallation(Context context, boolean isModPack, String modsPath, String modFileName, ModDetail modDetail, ModVersionItem modVersionItem) {
        // Doing this here since when starting installation, the progress does not start immediately
        // which may lead to two concurrent installations (very bad)
        ProgressLayout.setProgress(ProgressLayout.INSTALL_MODPACK, 0, R.string.generic_waiting);
        PojavApplication.sExecutorService.execute(() -> {
            try {
                ModLoader loaderInfo = installMod(isModPack, modsPath, modFileName, modDetail, modVersionItem);
                if (loaderInfo == null) return;
                loaderInfo.getDownloadTask(new NotificationDownloadListener(context, loaderInfo)).run();
            }catch (IOException e) {
                Tools.showErrorRemote(context, isModPack ? R.string.modpack_install_download_failed : R.string.profile_mods_download_mod_failed, e);
            }
        });
    }

    /**
     * Install the mod(pack).
     * May require the download of additional files.
     * May requires launching the installation of a modloader
     * @param modDetail The mod detail data
     * @param modVersionItem The selected version
     */
    ModLoader installMod(boolean isModPack, String modsPath, String modFileName, ModDetail modDetail, ModVersionItem modVersionItem) throws IOException;
}
