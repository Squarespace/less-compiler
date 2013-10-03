package com.squarespace.v6.template.less.exec;

import static com.squarespace.v6.template.less.ExecuteErrorType.GENERAL;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;

import java.util.HashMap;
import java.util.Map;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Stylesheet;


public class ImportCache {

  private final Map<String, Stylesheet> importMap = new HashMap<>();
  
  public ImportCache() {
  }
  
  public Stylesheet get(ExecEnv env, String path, boolean once) throws LessException {
    Stylesheet sheet = importMap.get(path);
    if (sheet != null) {
      if (once) {
        throw new LessException(error(GENERAL).arg0(path + " already imported once"));
      }
      return sheet;
    }
    Context ctx = env.context();
    sheet = ctx.parseImport(path);
    importMap.put(path, sheet);
    return sheet;
  }
  
}
