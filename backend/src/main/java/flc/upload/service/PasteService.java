package flc.upload.service;

import flc.upload.model.Paste;
import flc.upload.model.Result;

public interface PasteService {
    Result delete(Integer id) throws Exception;

    Result update(Paste paste) throws Exception;

    void deleteExpired() throws Exception;

    Result findAll() throws Exception;

    Result findById(Integer id) throws Exception;

    Result<Object> findLast() throws Exception;

    Result add(Paste paste) throws Exception;

    Result findUnlisted() throws Exception;
}
