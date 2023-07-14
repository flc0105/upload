package flc.upload.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import flc.upload.model.Paste;
import flc.upload.model.Result;

public interface PasteService {
    Result<?> add(Paste paste) throws Exception;

    Result<?> delete(Integer id) throws Exception;

    Result<?> update(Paste paste) throws Exception;

    Result<?> findAll() throws Exception;

    Result<?> findById(Integer id) throws Exception;

    Result<?> findLast() throws Exception;

    Result<?> findPrivate() throws Exception;

    Result<?> importPastes(String json) throws JsonProcessingException;

    void deleteExpired() throws Exception;

}
