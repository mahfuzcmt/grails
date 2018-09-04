package com.webcommander.util

import com.webcommander.manager.HookManager
import com.webcommander.throwables.AttachmentExistanceException

/**
 * Created by zobair on 23/02/14.*/
class TrashUtil {
    public static def preProcessFinalDelete(String hookPrefix, Object ref, Boolean at2_force, Boolean at1_force) {
        Map responseMap = [:];
        Map vetos = HookManager.hook(hookPrefix + "-delete-veto", [:], ref)
        if(vetos.size()) {
            responseMap.at3 = vetos
        }
        if(!at2_force) {
            Map at2s = HookManager.hook(hookPrefix + "-delete-at2-count", [:], ref)
            if(at2s.size()) {
                responseMap.at2 = at2s;
            }
        }
        if(!at1_force) {
            Map at1s = HookManager.hook(hookPrefix + "-delete-at1-count", [:], ref)
            if(at1s.size()) {
                responseMap.at1 = at1s;
            }
        }
        if(responseMap.size()) {
            throw new AttachmentExistanceException(responseMap)
        }
    }

    public static def preProcessPutInTrash(String hookPrefix, Object ref, Boolean at2_force, Boolean at1_force) {
        Map responseMap = [:];
        Map vetos = HookManager.hook(hookPrefix + "-put-trash-veto-count", [:], ref)
        if(vetos.size()) {
            responseMap.at3 = vetos
        }
        if(!at2_force) {
            Map at2s = HookManager.hook(hookPrefix + "-put-trash-at2-count", [:], ref)
            if(at2s.size()) {
                responseMap.at2 = at2s;
            }
        }
        if(!at1_force) {
            Map at1s = HookManager.hook(hookPrefix + "-put-trash-at1-count", [:], ref)
            if(at1s.size()) {
                responseMap.at1 = at1s;
            }
        }
        if(responseMap.size()) {
            throw new AttachmentExistanceException(responseMap)
        }
    }
}
