package rp.fitkit.api.dto.logbook;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Een volledig dagelijks logboek, inclusief alle secties en links.",
        example = """
                        {
                          "logId": 1,
                          "logDate": "2025-09-03",
                          "sections": [
                            {
                              "sectionType": "MORNING",
                              "summary": "De dag begon goed, veel energie. Ik heb nagedacht over het nieuwe [project](log:2).",
                              "mood": "Energiek"
                            },
                            {
                              "sectionType": "EVENING",
                              "summary": "De avond was rustig. Teruggedacht aan de [ochtend](log:1) en hoe de dag verliep.",
                              "mood": "Voldaan"
                            }
                          ],
                          "outgoingLinks": [
                            {
                              "linkId": 1,
                              "anchorText": "project",
                              "targetEntityType": "DAILY_LOG",
                              "targetEntityId": 2,
                              "previewTitle": "Logboek voor 2025-09-04",
                              "previewSnippet": "Vandaag begonnen met het project. Het was uitdagender dan verwacht..."
                            }
                          ],
                          "incomingLinks": [
                            {
                              "linkId": 2,
                              "anchorText": "ochtend",
                              "targetEntityType": "DAILY_LOG",
                              "targetEntityId": 1,
                              "previewTitle": "Logboek voor 2025-09-03 (Evening)",
                              "previewSnippet": "...Teruggedacht aan de ochtend en hoe de dag verliep."
                            }
                          ]
                        }
    """)
public class FullLogbookDto {
    private Long logId;
    private LocalDate logDate;
    private List<LogSectionDto> sections;
    private List<LinkPreviewDto> outgoingLinks;
    private List<LinkPreviewDto> incomingLinks;
}

