package com.ai.assistance.metaagent.terminal;

import com.ai.assistance.metaagent.terminal.CommandExecutionEvent;
import com.ai.assistance.metaagent.terminal.SessionDirectoryEvent;

oneway interface ITerminalCallback {
    void onCommandExecutionUpdate(in CommandExecutionEvent event);
    void onSessionDirectoryChanged(in SessionDirectoryEvent event);
} 
