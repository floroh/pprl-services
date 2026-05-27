package de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.api;

import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.ApiClient;

import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.BatchPredictionRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.BatchPredictionResponse;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.ModelMetadata;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.PredictionRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.PredictionResponse;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.ResponseFitTrainingFitPost;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.SampleSelectionRequest;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.SampleSelectionResponse;
import de.unileipzig.dbs.pprl.service.linkageunit.services.clients.classifier.model.TrainingRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.15.0")
public class DefaultApi {
    private ApiClient apiClient;

    public DefaultApi() {
        this(new ApiClient());
    }

    public DefaultApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Fit
     * 
     * <p><b>200</b> - Model training
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param trainingRequest The trainingRequest parameter
     * @return ResponseFitTrainingFitPost
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec fitTrainingFitPostRequestCreation(@javax.annotation.Nonnull TrainingRequest trainingRequest) throws WebClientResponseException {
        Object postBody = trainingRequest;
        // verify the required parameter 'trainingRequest' is set
        if (trainingRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'trainingRequest' when calling fitTrainingFitPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "HTTPBearer" };

        ParameterizedTypeReference<ResponseFitTrainingFitPost> localVarReturnType = new ParameterizedTypeReference<ResponseFitTrainingFitPost>() {};
        return apiClient.invokeAPI("/training/fit", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Fit
     * 
     * <p><b>200</b> - Model training
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param trainingRequest The trainingRequest parameter
     * @return ResponseFitTrainingFitPost
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseFitTrainingFitPost> fitTrainingFitPost(@javax.annotation.Nonnull TrainingRequest trainingRequest) throws WebClientResponseException {
        ParameterizedTypeReference<ResponseFitTrainingFitPost> localVarReturnType = new ParameterizedTypeReference<ResponseFitTrainingFitPost>() {};
        return fitTrainingFitPostRequestCreation(trainingRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Fit
     * 
     * <p><b>200</b> - Model training
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param trainingRequest The trainingRequest parameter
     * @return ResponseEntity&lt;ResponseFitTrainingFitPost&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ResponseFitTrainingFitPost>> fitTrainingFitPostWithHttpInfo(@javax.annotation.Nonnull TrainingRequest trainingRequest) throws WebClientResponseException {
        ParameterizedTypeReference<ResponseFitTrainingFitPost> localVarReturnType = new ParameterizedTypeReference<ResponseFitTrainingFitPost>() {};
        return fitTrainingFitPostRequestCreation(trainingRequest).toEntity(localVarReturnType);
    }

    /**
     * Fit
     * 
     * <p><b>200</b> - Model training
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param trainingRequest The trainingRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec fitTrainingFitPostWithResponseSpec(@javax.annotation.Nonnull TrainingRequest trainingRequest) throws WebClientResponseException {
        return fitTrainingFitPostRequestCreation(trainingRequest);
    }

    /**
     * Get model metdata
     * 
     * <p><b>200</b> - Model metadata
     * @return ModelMetadata&lt;ModelMetadata&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getModelMetadataModelsMetadataGetRequestCreation() throws WebClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "HTTPBearer" };

        ParameterizedTypeReference<ModelMetadata> localVarReturnType = new ParameterizedTypeReference<ModelMetadata>() {};
        return apiClient.invokeAPI("/models/metadata", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get model metdata
     * 
     * <p><b>200</b> - Model metadata
     * @return ModelMetadata&lt;ModelMetadata&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Flux<ModelMetadata> getModelMetadataModelsMetadataGet() throws WebClientResponseException {
        ParameterizedTypeReference<ModelMetadata> localVarReturnType = new ParameterizedTypeReference<ModelMetadata>() {};
        return getModelMetadataModelsMetadataGetRequestCreation().bodyToFlux(localVarReturnType);
    }

    /**
     * Get model metdata
     * 
     * <p><b>200</b> - Model metadata
     * @return ResponseEntity&lt;ModelMetadata&lt;ModelMetadata&gt;&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<List<ModelMetadata>>> getModelMetadataModelsMetadataGetWithHttpInfo() throws WebClientResponseException {
        ParameterizedTypeReference<ModelMetadata> localVarReturnType = new ParameterizedTypeReference<ModelMetadata>() {};
        return getModelMetadataModelsMetadataGetRequestCreation().toEntityList(localVarReturnType);
    }

    /**
     * Get model metdata
     * 
     * <p><b>200</b> - Model metadata
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getModelMetadataModelsMetadataGetWithResponseSpec() throws WebClientResponseException {
        return getModelMetadataModelsMetadataGetRequestCreation();
    }

    /**
     * Get model metadata
     * 
     * <p><b>200</b> - Model metadata
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param modelName The modelName parameter
     * @param modelVersion The modelVersion parameter
     * @return ModelMetadata
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getModelMetadataModelsModelNameModelVersionMetadataGetRequestCreation(@javax.annotation.Nonnull String modelName, @javax.annotation.Nonnull String modelVersion) throws WebClientResponseException {
        Object postBody = null;
        // verify the required parameter 'modelName' is set
        if (modelName == null) {
            throw new WebClientResponseException("Missing the required parameter 'modelName' when calling getModelMetadataModelsModelNameModelVersionMetadataGet", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'modelVersion' is set
        if (modelVersion == null) {
            throw new WebClientResponseException("Missing the required parameter 'modelVersion' when calling getModelMetadataModelsModelNameModelVersionMetadataGet", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        pathParams.put("model_name", modelName);
        pathParams.put("model_version", modelVersion);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "HTTPBearer" };

        ParameterizedTypeReference<ModelMetadata> localVarReturnType = new ParameterizedTypeReference<ModelMetadata>() {};
        return apiClient.invokeAPI("/models/{model_name}/{model_version}/metadata", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Get model metadata
     * 
     * <p><b>200</b> - Model metadata
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param modelName The modelName parameter
     * @param modelVersion The modelVersion parameter
     * @return ModelMetadata
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ModelMetadata> getModelMetadataModelsModelNameModelVersionMetadataGet(@javax.annotation.Nonnull String modelName, @javax.annotation.Nonnull String modelVersion) throws WebClientResponseException {
        ParameterizedTypeReference<ModelMetadata> localVarReturnType = new ParameterizedTypeReference<ModelMetadata>() {};
        return getModelMetadataModelsModelNameModelVersionMetadataGetRequestCreation(modelName, modelVersion).bodyToMono(localVarReturnType);
    }

    /**
     * Get model metadata
     * 
     * <p><b>200</b> - Model metadata
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param modelName The modelName parameter
     * @param modelVersion The modelVersion parameter
     * @return ResponseEntity&lt;ModelMetadata&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<ModelMetadata>> getModelMetadataModelsModelNameModelVersionMetadataGetWithHttpInfo(@javax.annotation.Nonnull String modelName, @javax.annotation.Nonnull String modelVersion) throws WebClientResponseException {
        ParameterizedTypeReference<ModelMetadata> localVarReturnType = new ParameterizedTypeReference<ModelMetadata>() {};
        return getModelMetadataModelsModelNameModelVersionMetadataGetRequestCreation(modelName, modelVersion).toEntity(localVarReturnType);
    }

    /**
     * Get model metadata
     * 
     * <p><b>200</b> - Model metadata
     * <p><b>400</b> - Model not found or invalid version
     * <p><b>422</b> - Validation Error
     * @param modelName The modelName parameter
     * @param modelVersion The modelVersion parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getModelMetadataModelsModelNameModelVersionMetadataGetWithResponseSpec(@javax.annotation.Nonnull String modelName, @javax.annotation.Nonnull String modelVersion) throws WebClientResponseException {
        return getModelMetadataModelsModelNameModelVersionMetadataGetRequestCreation(modelName, modelVersion);
    }

    /**
     * Predict Batch
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param batchPredictionRequest The batchPredictionRequest parameter
     * @return BatchPredictionResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec predictBatchPredictionBatchPostRequestCreation(@javax.annotation.Nonnull BatchPredictionRequest batchPredictionRequest) throws WebClientResponseException {
        Object postBody = batchPredictionRequest;
        // verify the required parameter 'batchPredictionRequest' is set
        if (batchPredictionRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'batchPredictionRequest' when calling predictBatchPredictionBatchPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "HTTPBearer" };

        ParameterizedTypeReference<BatchPredictionResponse> localVarReturnType = new ParameterizedTypeReference<BatchPredictionResponse>() {};
        return apiClient.invokeAPI("/prediction/batch", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Predict Batch
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param batchPredictionRequest The batchPredictionRequest parameter
     * @return BatchPredictionResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<BatchPredictionResponse> predictBatchPredictionBatchPost(@javax.annotation.Nonnull BatchPredictionRequest batchPredictionRequest) throws WebClientResponseException {
        ParameterizedTypeReference<BatchPredictionResponse> localVarReturnType = new ParameterizedTypeReference<BatchPredictionResponse>() {};
        return predictBatchPredictionBatchPostRequestCreation(batchPredictionRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Predict Batch
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param batchPredictionRequest The batchPredictionRequest parameter
     * @return ResponseEntity&lt;BatchPredictionResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<BatchPredictionResponse>> predictBatchPredictionBatchPostWithHttpInfo(@javax.annotation.Nonnull BatchPredictionRequest batchPredictionRequest) throws WebClientResponseException {
        ParameterizedTypeReference<BatchPredictionResponse> localVarReturnType = new ParameterizedTypeReference<BatchPredictionResponse>() {};
        return predictBatchPredictionBatchPostRequestCreation(batchPredictionRequest).toEntity(localVarReturnType);
    }

    /**
     * Predict Batch
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param batchPredictionRequest The batchPredictionRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec predictBatchPredictionBatchPostWithResponseSpec(@javax.annotation.Nonnull BatchPredictionRequest batchPredictionRequest) throws WebClientResponseException {
        return predictBatchPredictionBatchPostRequestCreation(batchPredictionRequest);
    }

    /**
     * Predict
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param predictionRequest The predictionRequest parameter
     * @return PredictionResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec predictPredictionPostRequestCreation(@javax.annotation.Nonnull PredictionRequest predictionRequest) throws WebClientResponseException {
        Object postBody = predictionRequest;
        // verify the required parameter 'predictionRequest' is set
        if (predictionRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'predictionRequest' when calling predictPredictionPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "HTTPBearer" };

        ParameterizedTypeReference<PredictionResponse> localVarReturnType = new ParameterizedTypeReference<PredictionResponse>() {};
        return apiClient.invokeAPI("/prediction", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Predict
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param predictionRequest The predictionRequest parameter
     * @return PredictionResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<PredictionResponse> predictPredictionPost(@javax.annotation.Nonnull PredictionRequest predictionRequest) throws WebClientResponseException {
        ParameterizedTypeReference<PredictionResponse> localVarReturnType = new ParameterizedTypeReference<PredictionResponse>() {};
        return predictPredictionPostRequestCreation(predictionRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Predict
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param predictionRequest The predictionRequest parameter
     * @return ResponseEntity&lt;PredictionResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<PredictionResponse>> predictPredictionPostWithHttpInfo(@javax.annotation.Nonnull PredictionRequest predictionRequest) throws WebClientResponseException {
        ParameterizedTypeReference<PredictionResponse> localVarReturnType = new ParameterizedTypeReference<PredictionResponse>() {};
        return predictPredictionPostRequestCreation(predictionRequest).toEntity(localVarReturnType);
    }

    /**
     * Predict
     * 
     * <p><b>200</b> - Model prediction
     * <p><b>422</b> - Validation Error
     * @param predictionRequest The predictionRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec predictPredictionPostWithResponseSpec(@javax.annotation.Nonnull PredictionRequest predictionRequest) throws WebClientResponseException {
        return predictPredictionPostRequestCreation(predictionRequest);
    }

    /**
     * Select
     * 
     * <p><b>200</b> - Successful Response
     * <p><b>422</b> - Validation Error
     * @param sampleSelectionRequest The sampleSelectionRequest parameter
     * @return SampleSelectionResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec selectSelectionPostRequestCreation(@javax.annotation.Nonnull SampleSelectionRequest sampleSelectionRequest) throws WebClientResponseException {
        Object postBody = sampleSelectionRequest;
        // verify the required parameter 'sampleSelectionRequest' is set
        if (sampleSelectionRequest == null) {
            throw new WebClientResponseException("Missing the required parameter 'sampleSelectionRequest' when calling selectSelectionPost", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<String, Object>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "HTTPBearer" };

        ParameterizedTypeReference<SampleSelectionResponse> localVarReturnType = new ParameterizedTypeReference<SampleSelectionResponse>() {};
        return apiClient.invokeAPI("/selection", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Select
     * 
     * <p><b>200</b> - Successful Response
     * <p><b>422</b> - Validation Error
     * @param sampleSelectionRequest The sampleSelectionRequest parameter
     * @return SampleSelectionResponse
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<SampleSelectionResponse> selectSelectionPost(@javax.annotation.Nonnull SampleSelectionRequest sampleSelectionRequest) throws WebClientResponseException {
        ParameterizedTypeReference<SampleSelectionResponse> localVarReturnType = new ParameterizedTypeReference<SampleSelectionResponse>() {};
        return selectSelectionPostRequestCreation(sampleSelectionRequest).bodyToMono(localVarReturnType);
    }

    /**
     * Select
     * 
     * <p><b>200</b> - Successful Response
     * <p><b>422</b> - Validation Error
     * @param sampleSelectionRequest The sampleSelectionRequest parameter
     * @return ResponseEntity&lt;SampleSelectionResponse&gt;
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public Mono<ResponseEntity<SampleSelectionResponse>> selectSelectionPostWithHttpInfo(@javax.annotation.Nonnull SampleSelectionRequest sampleSelectionRequest) throws WebClientResponseException {
        ParameterizedTypeReference<SampleSelectionResponse> localVarReturnType = new ParameterizedTypeReference<SampleSelectionResponse>() {};
        return selectSelectionPostRequestCreation(sampleSelectionRequest).toEntity(localVarReturnType);
    }

    /**
     * Select
     * 
     * <p><b>200</b> - Successful Response
     * <p><b>422</b> - Validation Error
     * @param sampleSelectionRequest The sampleSelectionRequest parameter
     * @return ResponseSpec
     * @throws WebClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec selectSelectionPostWithResponseSpec(@javax.annotation.Nonnull SampleSelectionRequest sampleSelectionRequest) throws WebClientResponseException {
        return selectSelectionPostRequestCreation(sampleSelectionRequest);
    }
}
