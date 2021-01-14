import os

def list_files(path, filename=None):
  fullname = path
  if filename:
    fullname = os.path.join(path, filename)
  files = []
  if os.path.isfile(fullname):
    return [fullname]
  
  elif os.path.isdir(fullname):
    for sub in os.listdir(fullname):
      files.extend(list_files(fullname, sub))
  
  return files
