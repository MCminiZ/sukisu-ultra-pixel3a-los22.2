#
# arch/arm64/Makefile
#
# This file is included by the global makefile so that you can add your own
# architecture-specific flags and dependencies.
#
# This file is subject to the terms and conditions of the GNU General Public
# License.  See the file "COPYING" in the main directory of this archive
# for more details.
#
# Copyright (C) 1995-2001 by Russell King

LDFLAGS_vmlinux	:=--no-undefined -X
CPPFLAGS_vmlinux.lds = -DTEXT_OFFSET=$(TEXT_OFFSET)
GZFLAGS		:=-9

ifeq ($(CONFIG_RELOCATABLE), y)
# Pass --no-apply-dynamic-relocs to restore pre-binutils-2.27 behaviour
# for relative relocs, since this leads to better Image compression
# with the relocation offsets always being zero.
LDFLAGS_vmlinux		+= -pie -shared -Bsymbolic \
			$(call ld-option, --no-apply-dynamic-relocs)
endif

ifeq ($(CONFIG_ARM64_ERRATUM_843419),y)
  ifeq ($(call ld-option, --fix-cortex-a53-843419),)
$(warning ld does not support --fix-cortex-a53-843419; kernel may be susceptible to erratum)
  else
    ifeq ($(call gold-ifversion, -lt, 114000000, y), y)
$(warning This version of GNU gold may generate incorrect code with --fix-cortex-a53-843419;\
	see https://sourceware.org/bugzilla/show_bug.cgi?id=21491)
    endif
LDFLAGS_vmlinux	+= --fix-cortex-a53-843419
  endif
else
  ifeq ($(ld-name),gold)
# Pass --no-fix-cortex-a53-843419 to ensure the erratum fix is disabled
LDFLAGS	+= --no-fix-cortex-a53-843419
  endif
endif

KBUILD_DEFCONFIG := defconfig

# Check for binutils support for specific extensions
lseinstr := $(call as-instr,.arch_extension lse,-DCONFIG_AS_LSE=1)

ifeq ($(CONFIG_ARM64_LSE_ATOMICS), y)
  ifeq ($(lseinstr),)
$(warning LSE atomics not supported by binutils)
  endif
endif

ifeq ($(CONFIG_COMPAT_VDSO), y)
  CROSS_COMPILE_ARM32 ?= $(CONFIG_CROSS_COMPILE_ARM32:"%"=%)

  # Check that the user has provided a valid prefix for the 32-bit toolchain.
  # To prevent selecting the system $(cc-name) by default, the prefix is not
  # allowed to be empty, unlike CROSS_COMPILE. In the unlikely event that the
  # system $(cc-name) is actually the 32-bit ARM compiler to be used, the
  # variable can be set to the dirname (e.g. CROSS_COMPILE_ARM32=/usr/bin/).
  # Note: this Makefile is read both before and after regenerating the config
  # (if needed). Any warning appearing before the config has been regenerated
  # should be ignored. If the error is triggered and you set
  # CONFIG_CROSS_COMPILE_ARM32, set CROSS_COMPILE_ARM32 to an appropriate value
  # when invoking make and fix CONFIG_CROSS_COMPILE_ARM32.
  ifeq ($(CROSS_COMPILE_ARM32),)
    $(error CROSS_COMPILE_ARM32 not defined or empty, the compat vDSO will not be built)
  else ifeq ($(cc-name),clang)
    export CLANG_TRIPLE_ARM32 ?= $(CROSS_COMPILE_ARM32)
    export CLANG_TARGET_ARM32 := --target=$(notdir $(CLANG_TRIPLE_ARM32:%-=%))
    export GCC_TOOLCHAIN32_DIR := $(dir $(shell which $(CROSS_COMPILE_ARM32)ld))
    export GCC_TOOLCHAIN32 := $(realpath $(GCC_TOOLCHAIN32_DIR)/..)
    export CLANG_PREFIX32 := --prefix=$(GCC_TOOLCHAIN32_DIR)
    export CLANG_GCC32_TC := --gcc-toolchain=$(GCC_TOOLCHAIN32)
    export CONFIG_VDSO32 := y
    vdso32 := -DCONFIG_VDSO32=1
  else ifeq ($(shell which $(CROSS_COMPILE_ARM32)$(cc-name) 2> /dev/null),)
    $(error $(CROSS_COMPILE_ARM32)$(cc-name) not found, check CROSS_COMPILE_ARM32)
  else
    export CROSS_COMPILE_ARM32
    export CONFIG_VDSO32 := y
    vdso32 := -DCONFIG_VDSO32=1
  endif
endif

ifeq ($(cc-name),clang)
# This is a workaround for https://bugs.llvm.org/show_bug.cgi?id=30792.
# TODO: revert when this is fixed in LLVM.
KBUILD_CFLAGS  += -mno-implicit-float
else
KBUILD_CFLAGS  += -mgeneral-regs-only
endif
KBUILD_CFLAGS	+= $(lseinstr) $(vdso32)
KBUILD_CFLAGS	+= -fno-asynchronous-unwind-tables
KBUILD_CFLAGS	+= $(call cc-option, -mpc-relative-literal-loads)
KBUILD_CFLAGS	+= -fno-pic
KBUILD_AFLAGS	+= $(lseinstr) $(vdso32)

ifeq ($(CONFIG_SHADOW_CALL_STACK), y)
KBUILD_CFLAGS	+= -ffixed-x18
endif

ifeq ($(CONFIG_CPU_BIG_ENDIAN), y)
KBUILD_CPPFLAGS	+= -mbig-endian
AS		+= -EB
LD		+= -EB
UTS_MACHINE	:= aarch64_be
else
KBUILD_CPPFLAGS	+= -mlittle-endian
AS		+= -EL
LD		+= -EL
UTS_MACHINE	:= aarch64
endif

CHECKFLAGS	+= -D__aarch64__

ifeq ($(CONFIG_ARM64_MODULE_CMODEL_LARGE), y)
KBUILD_CFLAGS_MODULE	+= -mcmodel=large
ifeq ($(CONFIG_LTO_CLANG), y)
# Code model is not stored in LLVM IR, so we need to pass it also to LLVMgold
KBUILD_LDFLAGS_MODULE	+= -plugin-opt=-code-model=large
endif
endif

ifeq ($(CONFIG_ARM64_MODULE_PLTS),y)
KBUILD_LDFLAGS_MODULE	+= -T $(srctree)/arch/arm64/kernel/module.lds
endif

# Default value
head-y		:= arch/arm64/kernel/head.o

# The byte offset of the kernel image in RAM from the start of RAM.
ifeq ($(CONFIG_ARM64_RANDOMIZE_TEXT_OFFSET), y)
TEXT_OFFSET := $(shell awk "BEGIN {srand(); printf \"0x%06x\n\", \
		 int(2 * 1024 * 1024 / (2 ^ $(CONFIG_ARM64_PAGE_SHIFT)) * \
		 rand()) * (2 ^ $(CONFIG_ARM64_PAGE_SHIFT))}")
else
TEXT_OFFSET := 0x00080000
endif

ifeq ($(cc-name),clang)
KBUILD_CFLAGS += $(call cc-disable-warning, asm-operand-widths)
endif

# KASAN_SHADOW_OFFSET = VA_START + (1 << (VA_BITS - 3)) - (1 << 61)
# in 32-bit arithmetic
KASAN_SHADOW_OFFSET := $(shell printf "0x%08x00000000\n" $$(( \
			(0xffffffff & (-1 << ($(CONFIG_ARM64_VA_BITS) - 32))) \
			+ (1 << ($(CONFIG_ARM64_VA_BITS) - 32 - 3)) \
			- (1 << (64 - 32 - 3)) )) )

export	TEXT_OFFSET GZFLAGS

core-y		+= arch/arm64/kernel/ arch/arm64/mm/
core-$(CONFIG_NET) += arch/arm64/net/
core-$(CONFIG_KVM) += arch/arm64/kvm/
core-$(CONFIG_XEN) += arch/arm64/xen/
core-$(CONFIG_CRYPTO) += arch/arm64/crypto/
libs-y		:= arch/arm64/lib/ $(libs-y)
core-$(CONFIG_EFI_STUB) += $(objtree)/drivers/firmware/efi/libstub/lib.a

suffix_$(CONFIG_KERNEL_GZIP) = gz
suffix_$(CONFIG_KERNEL_LZ4)  = lz4

# Default target when executing plain make
ifeq ($(CONFIG_BUILD_ARM64_APPENDED_DTB_IMAGE),y)
KBUILD_IMAGE	:= Image.$(suffix_y)-dtb
else
KBUILD_IMAGE	:= Image.$(suffix_y)
endif

KBUILD_DTBS	:= dtbs

KBUILD_DTBO_IMG := dtbo.img

ifeq ($(CONFIG_BUILD_ARM64_DT_OVERLAY),y)
export DTC_FLAGS := -@
endif

ifeq ($(CONFIG_BUILD_ARM64_APPLY_DTBO),y)
	KBUILD_APPLY_DTBO := apply_dtbo
endif

BUILD_DTC_PATH = $(subst $\",,$(CONFIG_BUILD_ARM64_DTC))
ifneq ($(BUILD_DTC_PATH),)
	DTC = $(BUILD_DTC_PATH)
endif

BUILD_DTC_FLAGS = $(subst $\",,$(CONFIG_BUILD_ARM64_DTC_FLAGS))
ifneq ($(BUILD_DTC_FLAGS),)
	DTC_FLAGS += $(BUILD_DTC_FLAGS)
endif

ifeq ($(CONFIG_BUILD_ARM64_DT_OVERLAY),y)
all:	$(KBUILD_IMAGE) $(KBUILD_DTBS) $(KBUILD_DTBO_IMG) $(KBUILD_APPLY_DTBO)
else
all:	$(KBUILD_IMAGE) $(KBUILD_DTBS)
endif

boot := arch/arm64/boot

Image: vmlinux
	$(Q)$(MAKE) $(build)=$(boot) $(boot)/$@

Image.%: Image
	$(Q)$(MAKE) $(build)=$(boot) $(boot)/$@

zinstall install:
	$(Q)$(MAKE) $(build)=$(boot) $@

%.dtb: scripts
	$(Q)$(MAKE) $(build)=$(boot)/dts $(boot)/dts/$@

PHONY += dtbs dtbs_install

dtbs: prepare scripts
	$(Q)$(MAKE) $(build)=$(boot)/dts

dtbs_install:
	$(Q)$(MAKE) $(dtbinst)=$(boot)/dts

Image-dtb Image.gz-dtb Image.lz4-dtb: vmlinux scripts dtbs
	$(Q)$(MAKE) $(build)=$(boot) $(boot)/$@

ifeq ($(CONFIG_BUILD_ARM64_DT_OVERLAY),y)
ifeq ($(CONFIG_BOARD_B1C1),y)
dtbo.img: $(boot)/dts/dtboimg.cfg dtbs
	$(Q)$(MAKE) $(build)=$(boot) $(boot)/$@
else
dtbo.img: $(boot)/dts/dtboimg-sdm670.cfg dtbs
	$(Q)$(MAKE) $(build)=$(boot) $(boot)/$@
endif
endif


ifeq ($(CONFIG_BUILD_ARM64_DT_OVERLAY),y)
PHONY += apply_dtbo
dtboapply := -f $(if $(KBUILD_SRC),$(srctree)/)scripts/Makefile.dtboapply obj
apply_dtbo: Image.lz4-dtb dtbo.img
	$(Q)$(MAKE) $(dtboapply)=$(boot) KERNEL_DTB=Image.lz4-dtb DTBO=dtbo.img
endif

PHONY += vdso_install
vdso_install:
	$(Q)$(MAKE) $(build)=arch/arm64/kernel/vdso $@

# We use MRPROPER_FILES and CLEAN_FILES now
archclean:
	$(Q)$(MAKE) $(clean)=$(boot)
	$(Q)$(MAKE) $(clean)=$(boot)/dts

CLEAN_FILES += $(boot)/Image $(boot)/Image-dtb $(boot)/Image.* $(boot)/dtbo.img
MRPROPER_FILES += $(boot)/Image $(boot)/Image-dtb $(boot)/Image.* $(boot)/dtbo.img

ifeq ($(KBUILD_EXTMOD),)
# We need to generate vdso-offsets.h before compiling certain files in kernel/.
# This ensures proper dependency ordering between prepare0, asm-offsets.h, and vdso-offsets.h
prepare: vdso_prepare

.NOTPARALLEL: vdso_prepare
vdso_prepare: prepare0
	@if [ "$(CONFIG_VDSO32)" = "y" ] && [ -z "$(CROSS_COMPILE_COMPAT)" ]; then \
		echo "Error: CROSS_COMPILE_COMPAT must be set for 32-bit VDSO"; exit 1; \
	fi
	$(Q)if ! $(MAKE) $(build)=arch/arm64/kernel/vdso include/generated/vdso-offsets.h; then \
		echo "Failed to generate 64-bit VDSO offsets"; exit 1; \
	fi
	$(if $(CONFIG_VDSO32), \
		$(Q)if ! $(MAKE) $(build)=arch/arm64/kernel/vdso32 include/generated/vdso32-offsets.h; then \
			echo "Failed to generate 32-bit VDSO offsets"; exit 1; \
		fi \
	)

# Explicit dependencies to prevent race conditions
arch/arm64/kernel/vdso/vdso.lds: include/generated/vdso-offsets.h
arch/arm64/kernel/vdso/vdso.so.dbg: include/generated/vdso-offsets.h
endif

define archhelp
  echo  '* Image.gz      - Compressed kernel image (arch/$(ARCH)/boot/Image.gz)'
  echo  '* Image.lz4      - Compressed kernel image (arch/$(ARCH)/boot/Image.lz4)'
  echo  '  Image         - Uncompressed kernel image (arch/$(ARCH)/boot/Image)'
  echo  '* dtbs          - Build device tree blobs for enabled boards'
  echo  '  dtbs_install  - Install dtbs to $(INSTALL_DTBS_PATH)'
  echo  '  install       - Install uncompressed kernel'
  echo  '  zinstall      - Install compressed kernel'
  echo  '                  Install using (your) ~/bin/installkernel or'
  echo  '                  (distribution) /sbin/installkernel or'
  echo  '                  install to $$(INSTALL_PATH) and run lilo'
endef
