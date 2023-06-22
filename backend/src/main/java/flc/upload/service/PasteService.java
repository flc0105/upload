package flc.upload.service;

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

    void deleteExpired() throws Exception;
}
