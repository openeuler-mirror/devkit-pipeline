import constant
from machine.scanner_machine import ScannerMachine
from machine.builder_machine import BuilderMachine
from machine.executor_machine import ExecutorMachine
from machine.devkit_machine import DevkitMachine

KLASS_DICT = {
    constant.EXECUTOR: ExecutorMachine,
    constant.DEVKIT: DevkitMachine,
    constant.SCANNER: ScannerMachine,
    constant.BUILDER: BuilderMachine,
}
