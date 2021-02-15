package org.recap.util.datadump;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCaseUT;
import org.recap.RecapConstants;
import org.recap.model.export.DataDumpRequest;
import org.recap.model.jpa.CollectionGroupEntity;
import org.recap.model.jpa.ETLRequestLogEntity;
import org.recap.model.jpa.ExportStatusEntity;
import org.recap.model.jpa.ImsLocationEntity;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.repository.ETLRequestLogDetailsRepository;
import org.recap.repository.ExportStatusDetailsRepository;
import org.recap.repository.ImsLocationDetailsRepository;
import org.recap.service.DataExportDBService;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;

public class DataDumpUtilUT extends BaseTestCaseUT {

    @InjectMocks
    DataDumpUtil dataDumpUtil;

    @Mock
    ExportStatusDetailsRepository exportStatusDetailsRepository;

    @Mock
    ETLRequestLogDetailsRepository etlRequestLogDetailsRepository;

    @Mock
    ImsLocationDetailsRepository imsLocationDetailsRepository;

    @Mock
    CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Mock
    DataExportDBService dataExportDBService;

    @Test
    public void setDataDumpRequest() {
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        String fetchType = "fetch";
        String institutionCodes = "PUL,CUL,NYPL";
        String date = new Date().toString();
        String toDate = new Date().toString();
        String collectionGroupIds = "2451";
        String transmissionType = "FULL";
        String requestingInstitutionCode = "1";
        String toEmailAddress = "test@gmail.com";
        String outputFormat = "xml";
        String imsDepositoryCodes = "2321";
        dataDumpUtil.setDataDumpRequest(dataDumpRequest, fetchType, institutionCodes, date, toDate, collectionGroupIds, transmissionType, requestingInstitutionCode, toEmailAddress, outputFormat, imsDepositoryCodes);
    }

    @Test
    public void setDataDumpRequestWithoutTramission() {
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        String fetchType = "fetch";
        String institutionCodes = "PUL,CUL,NYPL";
        String date = new Date().toString();
        String toDate = new Date().toString();
        String collectionGroupIds = null;
        String transmissionType = null;
        String requestingInstitutionCode = "1";
        String toEmailAddress = "test@gmail.com";
        String outputFormat = "xml";
        String imsDepositoryCodes = null;
        ImsLocationEntity imsLocationEntity = getImsLocationEntity();
        CollectionGroupEntity collectionGroupEntity = getCollectionGroupEntity();
        Mockito.when(imsLocationDetailsRepository.findByImsLocationCode(RecapConstants.IMS_DEPOSITORY_RECAP)).thenReturn(imsLocationEntity);
        Mockito.when(collectionGroupDetailsRepository.findByCollectionGroupCode(any())).thenReturn(collectionGroupEntity);
        dataDumpUtil.setDataDumpRequest(dataDumpRequest, fetchType, institutionCodes, date, toDate, collectionGroupIds, transmissionType, requestingInstitutionCode, toEmailAddress, outputFormat, imsDepositoryCodes);
    }

    @Test
    public void prepareRequestForAwaiting() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest();
        String status = "Complete";
        ExportStatusEntity exportStatusEntity = getExportStatusEntity();
        Mockito.when(exportStatusDetailsRepository.findByExportStatusCode(any())).thenReturn(exportStatusEntity);
        ETLRequestLogEntity etlRequestLogEntity = dataDumpUtil.prepareRequestForAwaiting(dataDumpRequest, status);
        assertNotNull(etlRequestLogEntity);
    }

    @Test
    public void updateStatusInETLRequestLogForFailure() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest();
        String outputString = RecapConstants.DATADUMP_EXPORT_FAILURE;
        ETLRequestLogEntity etlRequestLogEntity = getEtlRequestLogEntity();
        ExportStatusEntity exportStatusEntity = getExportStatusEntity();
        Mockito.when(etlRequestLogDetailsRepository.findById(dataDumpRequest.getEtlRequestId())).thenReturn(Optional.of(etlRequestLogEntity));
        Mockito.when(exportStatusDetailsRepository.findByExportStatusCode(any())).thenReturn(exportStatusEntity);
        dataDumpUtil.updateStatusInETLRequestLog(dataDumpRequest, outputString);
    }

    @Test
    public void updateStatusInETLRequestLogFor100() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest();
        String outputString = "100";
        ETLRequestLogEntity etlRequestLogEntity = getEtlRequestLogEntity();
        ExportStatusEntity exportStatusEntity = getExportStatusEntity();
        Mockito.when(etlRequestLogDetailsRepository.findById(dataDumpRequest.getEtlRequestId())).thenReturn(Optional.of(etlRequestLogEntity));
        Mockito.when(exportStatusDetailsRepository.findByExportStatusCode(any())).thenReturn(exportStatusEntity);
        dataDumpUtil.updateStatusInETLRequestLog(dataDumpRequest, outputString);
    }

    @Test
    public void updateStatusInETLRequestLog() {
        DataDumpRequest dataDumpRequest = getDataDumpRequest();
        String outputString = "test";
        ETLRequestLogEntity etlRequestLogEntity = getEtlRequestLogEntity();
        ExportStatusEntity exportStatusEntity = getExportStatusEntity();
        Mockito.when(etlRequestLogDetailsRepository.findById(dataDumpRequest.getEtlRequestId())).thenReturn(Optional.of(etlRequestLogEntity));
        Mockito.when(exportStatusDetailsRepository.findByExportStatusCode(any())).thenReturn(exportStatusEntity);
        dataDumpUtil.updateStatusInETLRequestLog(dataDumpRequest, outputString);
    }

    @Test
    public void checkAndPrepareAwaitingReqIfAny() {
        ExportStatusEntity exportStatusEntity = getExportStatusEntity();
        ETLRequestLogEntity etlRequestLogEntity = getEtlRequestLogEntity();
        Mockito.when(dataExportDBService.findByExportStatusCode(RecapConstants.AWAITING)).thenReturn(exportStatusEntity);
        Mockito.when(dataExportDBService.findAllStatusForS3OrderByRequestedTime(any(), any())).thenReturn(Arrays.asList(etlRequestLogEntity));
        DataDumpRequest dataDumpRequest = dataDumpUtil.checkAndPrepareAwaitingReqIfAny();
        assertNotNull(dataDumpRequest);
    }

    @Test
    public void checkAndPrepareAwaitingReqIfAnyWithEmptyLog() {
        ExportStatusEntity exportStatusEntity = getExportStatusEntity();
        Mockito.when(dataExportDBService.findByExportStatusCode(RecapConstants.AWAITING)).thenReturn(exportStatusEntity);
        Mockito.when(dataExportDBService.findAllStatusForS3OrderByRequestedTime(any(), any())).thenReturn(Collections.EMPTY_LIST);
        DataDumpRequest dataDumpRequest = dataDumpUtil.checkAndPrepareAwaitingReqIfAny();
        assertNull(dataDumpRequest);
    }

    @Test
    public void prepareRequestForExistingAwaiting() {
        ExportStatusEntity exportStatusEntity = getExportStatusEntity();
        ETLRequestLogEntity etlRequestLogEntity = getEtlRequestLogEntity();
        Mockito.when(dataExportDBService.findByExportStatusCode(RecapConstants.AWAITING)).thenReturn(exportStatusEntity);
        Mockito.when(dataExportDBService.findAllStatusForS3OrderByRequestedTime(exportStatusEntity.getId(), RecapConstants.DATADUMP_TRANSMISSION_TYPE_S3)).thenReturn(Arrays.asList(etlRequestLogEntity));
        DataDumpRequest dataDumpRequest = dataDumpUtil.prepareRequestForExistingAwaiting();
        assertNotNull(dataDumpRequest);
    }

    private ETLRequestLogEntity getEtlRequestLogEntity() {
        ETLRequestLogEntity etlRequestLogEntity = new ETLRequestLogEntity();
        etlRequestLogEntity.setId(1);
        etlRequestLogEntity.setExportStatusEntity(new ExportStatusEntity());
        etlRequestLogEntity.setCompleteTime(new Date());
        etlRequestLogEntity.setCollectionGroupIds("001,002,003");
        etlRequestLogEntity.setEmailIds("emailids");
        etlRequestLogEntity.setRequestedTime(new Date());
        etlRequestLogEntity.setFetchType("Pull");
        etlRequestLogEntity.setExportStatusId(1);
        etlRequestLogEntity.setImsRepositoryCodes("IMRC");
        etlRequestLogEntity.setMessage("msg");
        etlRequestLogEntity.setInstCodeToExport("ECExport");
        etlRequestLogEntity.setOutputFormat("Format");
        etlRequestLogEntity.setRequestingInstCode("22");
        etlRequestLogEntity.setUserName("test");
        etlRequestLogEntity.setTransmissionType("transmission");
        etlRequestLogEntity.setProvidedDate(new Date());
        return etlRequestLogEntity;
    }

    private ExportStatusEntity getExportStatusEntity() {
        ExportStatusEntity exportStatusEntity = new ExportStatusEntity();
        exportStatusEntity.setId(1);
        exportStatusEntity.setExportStatusCode("Complete");
        exportStatusEntity.setExportStatusDesc("Complete");
        return exportStatusEntity;
    }

    private CollectionGroupEntity getCollectionGroupEntity() {
        CollectionGroupEntity collectionGroupEntity = new CollectionGroupEntity();
        collectionGroupEntity.setId(1);
        collectionGroupEntity.setCollectionGroupCode("Complete");
        collectionGroupEntity.setCollectionGroupDescription("Complete");
        return collectionGroupEntity;
    }

    private ImsLocationEntity getImsLocationEntity() {
        ImsLocationEntity imsLocationEntity = new ImsLocationEntity();
        imsLocationEntity.setId(1);
        imsLocationEntity.setImsLocationCode("HD");
        imsLocationEntity.setImsLocationName("HD");
        return imsLocationEntity;
    }

    private DataDumpRequest getDataDumpRequest() {
        DataDumpRequest dataDumpRequest = new DataDumpRequest();
        dataDumpRequest.setFetchType("0");
        dataDumpRequest.setRequestingInstitutionCode("NYPL");
        List<Integer> cgIds = new ArrayList<>();
        cgIds.add(1);
        cgIds.add(2);
        dataDumpRequest.setCollectionGroupIds(cgIds);
        List<String> institutionCodes = new ArrayList<>();
        institutionCodes.add("CUL");
        institutionCodes.add("NYPL");
        dataDumpRequest.setInstitutionCodes(institutionCodes);
        dataDumpRequest.setTransmissionType("2");
        dataDumpRequest.setOutputFileFormat(RecapConstants.XML_FILE_FORMAT);
        dataDumpRequest.setDateTimeString(getDateTimeString());
        dataDumpRequest.setEtlRequestId(1);
        return dataDumpRequest;
    }

    private String getDateTimeString() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(RecapConstants.DATE_FORMAT_DDMMMYYYYHHMM);
        return sdf.format(date);
    }
}