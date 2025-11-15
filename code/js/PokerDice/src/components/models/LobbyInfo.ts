export class LobbiesInfoPayload {
  lobbies: Array<LobbyInfo>

  constructor(data: any) {
    // data é um array vindo do backend: [ { ...lobby }, { ... } ]
    this.lobbies = data.map((item: any) => new LobbyInfo(item))
  }
}

export class LobbyInfo {
  id: number
  name: string
  description: string
  hostId: number
  minUsers: number
  maxUsers: number
  rounds: number
  minCreditToParticipate: number
  // Campos opcionais, caso exista essa info no backend
  isRunning?: boolean
  currentUsers?: number

  constructor(data: any) {
    this.id = data.id
    this.name = data.name
    this.description = data.description
    this.hostId = data.hostId
    this.minUsers = data.minUsers
    this.maxUsers = data.maxUsers
    this.rounds = data.rounds
    this.minCreditToParticipate = data.minCreditToParticipate
    this.isRunning = data.isRunning
    this.currentUsers = data.currentUsers
  }
}
