package flc.upload.service;

import flc.upload.model.Result;

public interface ShareCodeService {

    Result add(String path) throws Exception;

    Result delete(String code) throws Exception;

    Result findAll() throws Exception;

    Result findByCode(String code) throws Exception;
}
