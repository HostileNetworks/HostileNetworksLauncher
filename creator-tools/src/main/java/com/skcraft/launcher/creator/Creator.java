/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.creator;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.creator.controller.WelcomeController;
import com.skcraft.launcher.creator.dialog.WelcomeDialog;
import com.skcraft.launcher.creator.model.creator.CreatorConfig;
import com.skcraft.launcher.creator.model.creator.RecentEntry;
import com.skcraft.launcher.creator.model.creator.Workspace;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.Getter;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;

public class Creator {

    @Getter private final File dataDir;
    @Getter private final CreatorConfig config;
    @Getter private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    public Creator() {
        this.dataDir = new File(getExecutionPath());
        this.config = Persistence.load(new File(dataDir, "config.json"), CreatorConfig.class);

        // Remove deleted workspaces
        List<RecentEntry> recentEntries = config.getRecentEntries();
        Iterator<RecentEntry> it = recentEntries.iterator();
        while (it.hasNext()) {
            RecentEntry workspace = it.next();
            if (!Workspace.getWorkspaceFile(workspace.getPath()).exists()) {
                it.remove();
            }
        }
    }

    public void showWelcome() {
        WelcomeDialog dialog = new WelcomeDialog();
        WelcomeController controller = new WelcomeController(dialog, this, dataDir);
        controller.show();
        dialog.dispose();
    }

    private static File getFileChooseDefaultDir() {
        JFileChooser chooser = new JFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        return fsv.getDefaultDirectory();
    }

    public static void main(String[] args) throws Exception {
        Launcher.setupLogger();
        System.setProperty("skcraftLauncher.killWithoutConfirm", "true");

        final Creator creator = new Creator();

        SwingUtilities.invokeAndWait(() -> {
            SwingHelper.setSwingProperties("Modpack Creator");
            SwingHelper.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            try {
                creator.showWelcome();
            } catch (Exception e) {
                SwingHelper.showErrorDialog(null, "Failed to start the modpack creator program.", "Start Error", e);
            }
        });
    }
    
    private String getExecutionPath() {
        String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
        absolutePath = absolutePath.replaceAll("%20"," "); // Surely need to do this here
        return absolutePath;
    }

}
