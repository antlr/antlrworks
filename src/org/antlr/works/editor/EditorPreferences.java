/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package org.antlr.works.editor;

import edu.usfca.xj.appkit.app.XJApplication;
import edu.usfca.xj.appkit.app.XJPreferences;
import edu.usfca.xj.foundation.XJSystem;

import java.awt.*;
import java.util.Calendar;
import java.util.Map;

public class EditorPreferences {

    // General
    public static final String PREF_STARTUP_ACTION = "PREF_STARTUP_ACTION";
    public static final String PREF_CONSOLE_SHOW = "PREF_CONSOLE_SHOW";
    public static final String PREF_LOOK_AND_FEEL = "PREF_LOOK_AND_FEEL";

    public static final int STARTUP_NEW_DOC = 0;
    public static final int STARTUP_OPEN_LAST_DOC = 1;

    // Editor
    public static final String PREF_TAB_WIDTH = "PREF_TAB_WIDTH";
    public static final String PREF_AUTOSAVE_ENABLED = "PREF_AUTOSAVE_ENABLED";
    public static final String PREF_AUTOSAVE_DELAY = "PREF_AUTOSAVE_DELAY";
    public static final String PREF_HIGHLIGHTCURSORLINE = "PREF_HIGHLIGHTCURSORLINE";
    public static final String PREF_EDITOR_FONT = "PREF_EDITOR_FONT";
    public static final String PREF_EDITOR_FONT_SIZE = "PREF_EDITOR_FONT_SIZE";
    public static final String PREF_EDITOR_FOLDING = "PREF_EDITOR_FOLDING";
    public static final String PREF_ACTIONS_ANCHORS_FOLDING = "PREF_ACTIONS_ANCHORS_FOLDING";
    public static final String PREF_PARSER_DELAY = "PREF_PARSER_DELAY";

    public static final int DEFAULT_TAB_WIDTH = 8;
    public static String DEFAULT_EDITOR_FONT;
    public static final int DEFAULT_EDITOR_FONT_SIZE = 12;
    public static final boolean DEFAULT_CONSOLE_SHOW = false;
    public static final boolean DEFAULT_EDITOR_FOLDING = true;
    public static final boolean DEFAULT_ACTIONS_ANCHORS_FOLDING = true;
    public static final int DEFAULT_PARSER_DELAY = 250;

    // Visualization
    public static final String PREF_DOT_TOOL_PATH = "PREF_DOT_TOOL_PATH";
    public static final String PREF_DOT_IMAGE_FORMAT = "PREF_DOT_IMAGE_FORMAT";

    public static final String DEFAULT_DOT_TOOL_PATH;
    public static final String DEFAULT_DOT_IMAGE_FORMAT = "png";

    // SCM - Perforce
    public static final String PREF_SCM_P4_ENABLED = "PREF_SCM_ENABLE_P4";
    public static final String PREF_SCM_P4_PORT = "PREF_SCM_P4_PORT";
    public static final String PREF_SCM_P4_USER = "PREF_SCM_P4_USER";
    public static final String PREF_SCM_P4_PASSWORD = "PREF_SCM_P4_PASSWORD";
    public static final String PREF_SCM_P4_CLIENT = "PREF_SCM_P4_CLIENT";
    public static final String PREF_SCM_P4_EXEC = "PREF_SCM_P4_EXEC";

    // Compiler
    public static final String PREF_JAVAC_CUSTOM_PATH = "PREF_JAVAC_CUSTOM_PATH";
    public static final String PREF_JAVAC_PATH = "PREF_JAVAC_PATH";
    public static final String PREF_JIKES_PATH = "PREF_JIKES_PATH";
    public static final String PREF_COMPILER = "PREF_COMPILER";

    public static final boolean DEFAULT_JAVAC_CUSTOM_PATH = false;
    public static final String DEFAULT_JAVAC_PATH = "";
    public static final String DEFAULT_JIKES_PATH = "";
    public static final String DEFAULT_COMPILER = "javac";

    public static final String COMPILER_JAVAC = "javac";
    public static final String COMPILER_JIKES = "jikes";
    public static final String COMPILER_INTEGRATED = "integrated";

    // Statistics
    public static final String PREF_STATS_REMINDER_METHOD = "PREF_STATS_REMINDER_METHOD";
    public static final String PREF_STATS_REMINDER_NEXT_DATE = "PREF_STATS_REMINDER_NEXT_DATE";

    public static final int STATS_REMINDER_MANUALLY = 0;
    public static final int STATS_REMINDER_WEEKLY = 1;

    public static final int DEFAULT_STATS_REMINDER_METHOD = STATS_REMINDER_WEEKLY;

    // Updates
    public static final String PREF_UPDATE_TYPE = "PREF_UPDATE_TYPE";
    public static final String PREF_DOWNLOAD_PATH = "PREF_DOWNLOAD_PATH";
    public static final String PREF_UPDATE_NEXT_DATE = "PREF_UPDATE_NEXT_DATE";

    public static final int UPDATE_MANUALLY = 0;
    public static final int UPDATE_AT_STARTUP = 1;
    public static final int UPDATE_DAILY = 2;
    public static final int UPDATE_WEEKLY = 3;

    public static final int DEFAULT_UPDATE_TYPE = UPDATE_WEEKLY;
    public static final String DEFAULT_DOWNLOAD_PATH = System.getProperty("user.home");

    // Colors
    private static Color[] colors = { Color.BLACK, Color.BLUE, Color.CYAN, Color.DARK_GRAY, Color.GRAY, Color.GREEN,
                                        Color.LIGHT_GRAY, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED, Color.WHITE, Color.YELLOW};

    public static final String PREF_NONCONSUMED_TOKEN_COLOR = "PREF_NONCONSUMED_TOKEN_COLOR";
    public static final int DEFAULT_NONCONSUMED_TOKEN_COLOR = 6;    // light dray

    public static final String PREF_CONSUMED_TOKEN_COLOR = "PREF_CONSUMED_TOKEN_COLOR";
    public static final int DEFAULT_CONSUMED_TOKEN_COLOR = 0;    // black

    public static final String PREF_HIDDEN_TOKEN_COLOR = "PREF_HIDDEN_TOKEN_COLOR";
    public static final int DEFAULT_HIDDEN_TOKEN_COLOR = 6;    // light dray

    public static final String PREF_DEAD_TOKEN_COLOR = "PREF_DEAD_TOKEN_COLOR";
    public static final int DEFAULT_DEAD_TOKEN_COLOR = 10;    // red

    public static final String PREF_LOOKAHEAD_TOKEN_COLOR = "PREF_LOOKAHEAD_TOKEN_COLOR";
    public static final int DEFAULT_LOOKAHEAD_TOKEN_COLOR = 1;    // blue

    // Other
    public static final String PREF_USER_REGISTERED = "PREF_USER_REGISTERED";
    public static final String PREF_SERVER_ID = "PREF_SERVER_ID";

    public static final String PREF_OUTPUT_PATH = "PREF_OUTPUT_PATH";
    public static final String PREF_START_SYMBOL = "PREF_START_SYMBOL";
    public static final String PREF_DEBUG_BREAK_EVENT = "PREF_DEBUG_BREAK_EVENT";

    public static final String PREF_PERSONAL_INFO = "PREF_OUTPUT_DEV_DATE";
    public static final String PREF_PRIVATE_MENU = "PREF_PRIVATE_MENU";

    public static final String DEFAULT_OUTPUT_PATH;

    static {
        DEFAULT_EDITOR_FONT = "Courier New";

        if(XJSystem.isMacOS()) {
            DEFAULT_OUTPUT_PATH = "/tmp/antlrworks/";
            DEFAULT_DOT_TOOL_PATH = "/Applications/Graphviz.app/Contents/MacOS/dot";
            if(Font.getFont("Monospaced") != null)
                DEFAULT_EDITOR_FONT = "Monospaced";
        } else if(XJSystem.isWindows()) {
            DEFAULT_OUTPUT_PATH = "\\tmp\\antlrworks\\";
            DEFAULT_DOT_TOOL_PATH = "";
            if(Font.getFont("Tahoma") != null)
                DEFAULT_EDITOR_FONT = "Tahoma";
        } else if(XJSystem.isLinux()) {
            DEFAULT_OUTPUT_PATH = "/tmp/antlrworks/";
            DEFAULT_DOT_TOOL_PATH = "";
            if(Font.getFont("Monospaced") != null)
                DEFAULT_EDITOR_FONT = "Monospaced";
        } else {
            DEFAULT_OUTPUT_PATH = "/tmp/antlrworks/";
            DEFAULT_DOT_TOOL_PATH = "";
            if(Font.getFont("Courier") != null)
                DEFAULT_EDITOR_FONT = "Courier";
        }
    }

    public static void setOutputPath(String path) {
        getPreferences().setString(PREF_OUTPUT_PATH, path);
    }

    public static String getOutputPath() {
        return getPreferences().getString(PREF_OUTPUT_PATH, DEFAULT_OUTPUT_PATH);
    }

    public static void setStartSymbol(String startSymbol) {
        getPreferences().setString(PREF_START_SYMBOL, startSymbol);
    }

    public static String getStartSymbol() {
        return getPreferences().getString(PREF_START_SYMBOL, "");
    }

    public static int getStartupAction() {
        return getPreferences().getInt(PREF_STARTUP_ACTION, STARTUP_OPEN_LAST_DOC);
    }

    public static boolean getConsoleShow() {
        return getPreferences().getBoolean(PREF_CONSOLE_SHOW, DEFAULT_CONSOLE_SHOW);
    }

    public static boolean getAutoSaveEnabled() {
        return getPreferences().getBoolean(PREF_AUTOSAVE_ENABLED, false);
    }

    public static int getAutoSaveDelay() {
        return getPreferences().getInt(PREF_AUTOSAVE_DELAY, 5);
    }

    public static boolean getHighlightCursorEnabled() {
        return getPreferences().getBoolean(PREF_HIGHLIGHTCURSORLINE, true);
    }

    public static int getEditorTabSize() {
        return getPreferences().getInt(PREF_TAB_WIDTH, DEFAULT_TAB_WIDTH);
    }

    public static String getEditorFont() {
        return getPreferences().getString(PREF_EDITOR_FONT, DEFAULT_EDITOR_FONT);
    }

    public static int getEditorFontSize() {
        return getPreferences().getInt(PREF_EDITOR_FONT_SIZE, DEFAULT_EDITOR_FONT_SIZE);
    }

    public static boolean getFoldingEnabled() {
        return getPreferences().getBoolean(PREF_EDITOR_FOLDING, DEFAULT_EDITOR_FOLDING);
    }

    public static boolean getDisplayActionsAnchorsFolding() {
        return getPreferences().getBoolean(PREF_ACTIONS_ANCHORS_FOLDING, DEFAULT_ACTIONS_ANCHORS_FOLDING);        
    }

    public static int getParserDelay() {
        return getPreferences().getInt(PREF_PARSER_DELAY, DEFAULT_PARSER_DELAY);
    }

    public static String getLookAndFeel() {
        return getPreferences().getString(PREF_LOOK_AND_FEEL, null);
    }

    public static void setDOTToolPath(String path) {
        getPreferences().setString(PREF_DOT_TOOL_PATH, path);
    }

    public static String getDOTToolPath() {
        return getPreferences().getString(PREF_DOT_TOOL_PATH, DEFAULT_DOT_TOOL_PATH);
    }

    public static String getDOTImageFormat() {
        return getPreferences().getString(PREF_DOT_IMAGE_FORMAT, DEFAULT_DOT_IMAGE_FORMAT);
    }

    public static boolean getP4Enabled() {
        return getPreferences().getBoolean(PREF_SCM_P4_ENABLED, false);
    }

    public static String getP4Port() {
        return getPreferences().getString(PREF_SCM_P4_PORT, "");
    }

    public static String getP4User() {
        return getPreferences().getString(PREF_SCM_P4_USER, "");
    }

    public static String getP4Password() {
        return getPreferences().getString(PREF_SCM_P4_PASSWORD, "");
    }

    public static String getP4Client() {
        return getPreferences().getString(PREF_SCM_P4_CLIENT, "");
    }

    public static String getP4ExecPath() {
        return getPreferences().getString(PREF_SCM_P4_EXEC, "");
    }

    public static boolean getJavaCCustomPath() {
        return getPreferences().getBoolean(PREF_JAVAC_CUSTOM_PATH, DEFAULT_JAVAC_CUSTOM_PATH);
    }

    public static void setJavaCPath(String path) {
        getPreferences().setString(PREF_JAVAC_PATH, path);
    }

    public static String getJavaCPath() {
        return getPreferences().getString(PREF_JAVAC_PATH, DEFAULT_JAVAC_PATH);
    }

    public static void setJikesPath(String path) {
        getPreferences().setString(PREF_JIKES_PATH, path);
    }

    public static String getJikesPath() {
        return getPreferences().getString(PREF_JIKES_PATH, DEFAULT_JIKES_PATH);
    }

    public static String getCompiler() {
        return getPreferences().getString(PREF_COMPILER, DEFAULT_COMPILER);
    }

    public static int getStatsReminderType() {
        return getPreferences().getInt(PREF_STATS_REMINDER_METHOD, DEFAULT_STATS_REMINDER_METHOD);
    }

    public static void setStatsReminderNextDate(Calendar date) {
        getPreferences().setObject(PREF_STATS_REMINDER_NEXT_DATE, date);
    }

    public static Calendar getStatsReminderNextDate() {
        return (Calendar)getPreferences().getObject(PREF_STATS_REMINDER_NEXT_DATE, null);
    }

    public static int getUpdateType() {
        return getPreferences().getInt(PREF_UPDATE_TYPE, DEFAULT_UPDATE_TYPE);
    }

    public static void setUpdateNextDate(Calendar date) {
        getPreferences().setObject(PREF_UPDATE_NEXT_DATE, date);
    }

    public static Calendar getUpdateNextDate() {
        return (Calendar)getPreferences().getObject(PREF_UPDATE_NEXT_DATE, null);
    }

    public static void setDownloadPath(String path) {
        getPreferences().setString(PREF_DOWNLOAD_PATH, path);
    }

    public static String getDownloadPath() {
        return getPreferences().getString(PREF_DOWNLOAD_PATH, DEFAULT_DOWNLOAD_PATH);
    }

    public static void setUserRegistered(boolean flag) {
        getPreferences().setBoolean(PREF_USER_REGISTERED, flag);
    }

    public static boolean isUserRegistered() {
        return getPreferences().getBoolean(PREF_USER_REGISTERED, false);
    }

    public static void removeUserRegistration() {
        getPreferences().remove(PREF_USER_REGISTERED);
    }

    public static void setServerID(String id) {
        getPreferences().setString(PREF_SERVER_ID, id);
    }

    public static String getServerID() {
        return getPreferences().getString(PREF_SERVER_ID, null);
    }

    public static void setPersonalInfo(Map info) {
        getPreferences().setObject(PREF_PERSONAL_INFO, info);
    }

    public static Map getPersonalInfo() {
        return (Map)getPreferences().getObject(PREF_PERSONAL_INFO, null);
    }

    public static boolean getPrivateMenu() {
        return getPreferences().getBoolean(PREF_PRIVATE_MENU, false);
    }

    public static Color getNonConsumedTokenColor() {
        return colors[getPreferences().getInt(PREF_NONCONSUMED_TOKEN_COLOR, DEFAULT_NONCONSUMED_TOKEN_COLOR)];
    }

    public static Color getConsumedTokenColor() {
        return colors[getPreferences().getInt(PREF_CONSUMED_TOKEN_COLOR, DEFAULT_CONSUMED_TOKEN_COLOR)];
    }

    public static Color getHiddenTokenColor() {
        return colors[getPreferences().getInt(PREF_HIDDEN_TOKEN_COLOR, DEFAULT_HIDDEN_TOKEN_COLOR)];
    }

    public static Color getDeadTokenColor() {
        return colors[getPreferences().getInt(PREF_DEAD_TOKEN_COLOR, DEFAULT_DEAD_TOKEN_COLOR)];
    }

    public static Color getLookaheadTokenColor() {
        return colors[getPreferences().getInt(PREF_LOOKAHEAD_TOKEN_COLOR, DEFAULT_LOOKAHEAD_TOKEN_COLOR)];
    }

    public static XJPreferences getPreferences() {
        return XJApplication.shared().getPreferences();
    }

}
