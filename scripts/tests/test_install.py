import importlib.util
from pathlib import Path
import subprocess
import unittest
spec=importlib.util.spec_from_file_location("installer",Path(__file__).resolve().parents[1]/"install.py")
installer=importlib.util.module_from_spec(spec);spec.loader.exec_module(installer)

class InstalledApkPathsTest(unittest.TestCase):
    def result(self,code,out="",err=""):
        return subprocess.CompletedProcess([],code,out,err)
    def test_new_device_has_no_existing_apks(self):
        self.assertEqual([],installer.installed_apk_paths(self.result(1)))
    def test_preserves_all_existing_splits(self):
        self.assertEqual(["/data/app/base.apk","/data/app/split.apk"],installer.installed_apk_paths(self.result(0,"package:/data/app/base.apk\npackage:/data/app/split.apk\n")))
    def test_query_errors_stop_before_install(self):
        for value in [self.result(1,"","error: device offline"),self.result(0,"unrecognized output"),self.result(2),self.result(1,"package:/data/app/base.apk")]:
            with self.assertRaises(RuntimeError):installer.installed_apk_paths(value)

if __name__=="__main__":unittest.main()
