#ifndef ____OMX_MALLOC_H____
#define ____OMX_MALLOC_H____

#include "vce_cfg.h"

/* enable_gralloc */
#ifdef enable_gralloc
#include "OMX_Types.h"
#include "OMX_Core.h"
#include "Igralloc.h"   
#define OMX_VCE_GRALLOC_USAGE  GRALLOC_USAGE_SW_READ_OFTEN
#endif

void *omx_malloc_phy(int len, unsigned long *vir_addr);
void omx_free_phy(void *phy_addr);
void *omx_mmap_ion_fd(int ion_fd, int length);
int omx_munmap_ion_fd(void *vir_addr, int length);

#ifdef enable_gralloc
OMX_ERRORTYPE VCE_OSAL_GetPhyAddr(OMX_PTR handle, OMX_PTR *paddr);
OMX_ERRORTYPE VCE_OSAL_GetBufInfo(OMX_PTR handle, OMX_PTR width, OMX_PTR height, OMX_PTR format, OMX_PTR size);
OMX_ERRORTYPE VCE_OSAL_LockANBHandleWidthUsage(OMX_PTR handle, OMX_U32 usage, OMX_PTR *vaddr);
OMX_ERRORTYPE VCE_OSAL_UnlockANBHandle(OMX_PTR handle);
#endif

#endif
