import logging
import os
import pathlib
from io import StringIO

import paramiko
from cryptography.hazmat.primitives import serialization as crypto_serialization
from cryptography.hazmat.primitives.asymmetric import ed25519, dsa, rsa, ec
from paramiko import PKey


class TransportException(Exception):
    def __init__(self, *args, **kwargs):
        super(TransportException, self).__init__(args, kwargs)


class SSHClientFactory:

    def __init__(self, ip, user, password=None, port=22, pkey_file=None, pkey_content=None, pkey_password=None):
        self.pkey_file = pkey_file
        self.pkey_content = pkey_content
        self.pkey_password = pkey_password
        self.password = password
        self.ip = ip
        self.port = port
        self.user = user

    def create_ssh_client(self):
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        try:
            if self.password:
                # 使用密码进行链接
                ssh.connect(hostname=self.ip, port=22, username=self.user, password=self.password)
            elif self.pkey_file or self.pkey_content:
                # 使用指定的免密文件路径或者免密内容进行链接（内容支持加密）
                pkey = self.__pkey()
                ssh.connect(hostname=self.ip, port=22, username=self.user, pkey=pkey)
            else:
                # 使用.ssh 下的私钥尝试链接
                try_result = False
                for pkey_path in self.__get_home_pkey_path():
                    try:
                        pkey = self.__pkey(pkey_path)
                        ssh.connect(hostname=self.ip, port=22, username=self.user, pkey=pkey)
                        try_result = True
                        break
                    except Exception as err:
                        logging.exception(err)
                if not try_result:
                    raise TransportException("The passwordless configuration is incorrect")
        except Exception as ex:
            logging.exception(ex)
            raise TransportException()
        return ssh

    @staticmethod
    def __get_home_pkey_path():
        pkey_files = list()
        base = str(pathlib.Path.home()) + "/.ssh"
        for filename in os.listdir(base):
            filepath = os.path.join(base, filename)
            if os.path.isfile(filepath) and filepath.startswith("id_") and not filepath.endswith("pub"):
                pkey_files.append(filepath)
        return pkey_files

    def __choose_pkey(self, pkey_path=None):
        if pkey_path:
            return self.__from_private_key_file(pkey_path)
        if self.pkey_file:
            return self.__from_private_key_file(self.pkey_file, password=self.pkey_password)
        if self.pkey_content:
            return self.__from_private_key_content(self.pkey_content, password=self.pkey_password)
        raise TransportException()

    def __pkey(self, pkey_path=None):
        try:
            # 指定本地的RSA私钥文件。如果建立密钥对时设置的有密码，password为设定的密码，如无不用指定password参数
            pkey = self.__choose_pkey(pkey_path)
        except (IOError,) as e:
            logging.error(f"Pkey file not exists. {str(e)}")
            raise TransportException()
        except (paramiko.ssh_exception.PasswordRequiredException, paramiko.ssh_exception.AuthenticationException) as e:
            logging.warning(f"Pkey password is required. {str(e)}")
            raise TransportException(str(e))
        except Exception as e:
            logging.error(f"Connect remote {self.ip} failed because of wrong pkey. {str(e)}")
            raise TransportException()
        else:
            return pkey

    def __from_private_key_file(self, pkey_file: str, password=None) -> PKey:
        with open(pkey_file, mode="r", encoding="utf-8") as file:
            key = self.__get_key_class(file.read())
        if isinstance(key, rsa.RSAPrivateKey):
            private_key = paramiko.RSAKey.from_private_key_file(pkey_file, password)
        elif isinstance(key, ed25519.Ed25519PrivateKey):
            private_key = paramiko.Ed25519Key.from_private_key_file(pkey_file, password)
        elif isinstance(key, ec.EllipticCurvePrivateKey):
            private_key = paramiko.ECDSAKey.from_private_key_file(pkey_file, password)
        elif isinstance(key, dsa.DSAPrivateKey):
            private_key = paramiko.DSSKey.from_private_key_file(pkey_file, password)
        else:
            raise TransportException()
        return private_key

    def __from_private_key_content(self, pkey_content: str, password=None) -> PKey:
        key = self.__get_key_class(pkey_content)
        if isinstance(key, rsa.RSAPrivateKey):
            private_key = paramiko.RSAKey.from_private_key(StringIO(pkey_content), password)
        elif isinstance(key, ed25519.Ed25519PrivateKey):
            private_key = paramiko.Ed25519Key.from_private_key(StringIO(pkey_content), password)
        elif isinstance(key, ec.EllipticCurvePrivateKey):
            private_key = paramiko.ECDSAKey.from_private_key(StringIO(pkey_content), password)
        elif isinstance(key, dsa.DSAPrivateKey):
            private_key = paramiko.DSSKey.from_private_key(StringIO(pkey_content), password)
        else:
            raise TransportException()
        return private_key

    def __get_key_class(self, pkey_content: str):
        file_bytes = bytes(pkey_content, "utf-8")
        pkey_pass = None
        if self.pkey_password:
            pkey_pass = bytes(self.pkey_password, "utf-8")
        try:
            return crypto_serialization.load_ssh_private_key(file_bytes, password=pkey_pass)
        except ValueError:
            return crypto_serialization.load_pem_private_key(file_bytes, password=pkey_pass)


if __name__ == "__main__":
    factory = SSHClientFactory("127.0.0.1", "root", pkey_content="""-----BEGIN OPENSSH PRIVATE KEY-----
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
Q0ktTVQBAgMEBQYH
-----END OPENSSH PRIVATE KEY-----""")
    ssh_client = factory.create_ssh_client()
    stdin, stdout, stderr = ssh_client.exec_command("whoami")
    print(stdout.read())
