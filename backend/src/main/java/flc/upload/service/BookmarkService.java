package flc.upload.service;

import flc.upload.model.Result;

import java.util.List;

public interface BookmarkService {

    Result add(String url) throws Exception;

    Result delete(Integer id) throws Exception;

    Result update(Integer id) throws Exception;

    Result findAll() throws Exception;

    Result bulkAdd(String data) throws Exception;

    Result updateAll() throws Exception;

    Result rename(Integer id, String title, String url) throws Exception;


    Result addTag(Integer bookmarkId, List<Integer> tagIds) throws Exception;

    Result findAllTags() throws Exception;


}
