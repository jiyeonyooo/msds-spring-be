package resv.service;

import resv.dto.CreateResvRequestDTO;
import resv.dto.CreateResvResponseDTO;

public interface ResvService {

    CreateResvResponseDTO create(String memberEmail, CreateResvRequestDTO request);
}
