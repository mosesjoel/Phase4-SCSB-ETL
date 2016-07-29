package org.recap.route;

import org.apache.commons.lang3.StringUtils;
import org.recap.model.jpa.XmlRecordEntity;
import org.recap.repository.XmlRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Created by rajeshbabuk on 18/7/16.
 */
@Component
public class EtlDataLoadProcessor {

    Logger logger = LoggerFactory.getLogger(EtlDataLoadProcessor.class);

    private Integer batchSize;
    private String fileName;
    private XmlRecordRepository xmlRecordRepository;
    private RecordProcessor recordProcessor;

    public void startLoadProcess() {
        List distinctFileNames = xmlRecordRepository.findDistinctFileNames();
        for (Iterator iterator = distinctFileNames.iterator(); iterator.hasNext(); ) {
            String distinctFileName = (String) iterator.next();
            if (distinctFileName.contains(fileName)) {
                long totalDocCount;
                totalDocCount = xmlRecordRepository.countByXmlFileNameContaining(distinctFileName);

                if (totalDocCount > 0) {
                    int quotient = Integer.valueOf(Long.toString(totalDocCount)) / (batchSize);
                    int remainder = Integer.valueOf(Long.toString(totalDocCount)) % (batchSize);

                    int loopCount = remainder == 0 ? quotient : quotient + 1;

                    Page<XmlRecordEntity> xmlRecordEntities = null;
                    long totalStartTime = System.currentTimeMillis();
                    for (int i = 0; i < loopCount; i++) {
                        long startTime = System.currentTimeMillis();
                        xmlRecordEntities = xmlRecordRepository.findByXmlFileName(new PageRequest(i, batchSize), distinctFileName);
                        recordProcessor.setXmlFileName(distinctFileName);
                        recordProcessor.process(xmlRecordEntities);
                        long endTime = System.currentTimeMillis();
                        logger.info("Time taken to save: " + xmlRecordEntities.getNumberOfElements() + " bibs and related data is: " + (endTime - startTime) / 1000 + " seconds.");
                    }


                    long totalEndTime = System.currentTimeMillis();
                    logger.info("Total time taken to save: " + xmlRecordEntities.getTotalElements() + " bibs and related data is: " + (totalEndTime - totalStartTime) / 1000 + " seconds.");
                } else {
                    logger.info("No records found to load into DB");
                }
            }
        }
        recordProcessor.shutdownExecutorService();
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public XmlRecordRepository getXmlRecordRepository() {
        return xmlRecordRepository;
    }

    public void setXmlRecordRepository(XmlRecordRepository xmlRecordRepository) {
        this.xmlRecordRepository = xmlRecordRepository;
    }

    public RecordProcessor getRecordProcessor() {
        return recordProcessor;
    }

    public void setRecordProcessor(RecordProcessor recordProcessor) {
        this.recordProcessor = recordProcessor;
    }
}
